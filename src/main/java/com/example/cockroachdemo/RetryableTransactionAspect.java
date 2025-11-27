package com.example.cockroachdemo;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicLong;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 可重试事务切面（RetryableTransactionAspect）：
 * - 拦截标注了 @Transactional 的方法（通过切点 anyTransactionBoundaryOperation）
 * - 如果因为并发冲突/瞬时数据访问（如
 * TransientDataAccessException/TransactionSystemException）导致失败，
 * 则会在非事务上下文中重试整个方法（确保事务不会被错误嵌套）
 * - 这种实现对于 CockroachDB 等分布式数据库很有用，因为它们会在并发冲突时返回重试指示（需要客户端重试）
 * 
 * 注意：
 * - 该切面必须在事务拦截器之前执行（@Order 确保它排在嵌套事务拦截器之前），以便从非事务上下文重试。
 * - 重试策略：默认最多 30 次尝试，起始退避时间 150ms，逐步增加至最大 1500ms（指数/线性退避混合）
 */
@Component
@Aspect
// Before TX advisor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class RetryableTransactionAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("@annotation(transactional)")
    public void anyTransactionBoundaryOperation(Transactional transactional) {
    }

    @Around(value = "anyTransactionBoundaryOperation(transactional)", argNames = "pjp,transactional")
    public Object retryableOperation(ProceedingJoinPoint pjp, Transactional transactional)
            throws Throwable {
        final int totalRetries = 30;
        int numAttempts = 0;
        AtomicLong backoffMillis = new AtomicLong(150);

        // 确保当前是非事务上下文（切面在事务之前被调用），这样可以在彻底回滚失败的事务后进行重试
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "TX active");

        do {
            try {
                numAttempts++;
                return pjp.proceed();
            } catch (TransientDataAccessException | TransactionSystemException ex) {
                // 捕获瞬时异常并应用重试逻辑与退避策略
                handleTransientException(ex, numAttempts, totalRetries, pjp, backoffMillis);
            } catch (UndeclaredThrowableException ex) {
                Throwable t = ex.getUndeclaredThrowable();
                // 如果被包装的异常是瞬时数据访问异常，按重试逻辑处理，否则向上抛出
                if (t instanceof TransientDataAccessException) {
                    handleTransientException(t, numAttempts, totalRetries, pjp, backoffMillis);
                } else {
                    throw ex;
                }
            }
        } while (numAttempts < totalRetries);

        // 超过最大重试次数仍然失败时抛出 ConcurrencyFailureException
        throw new ConcurrencyFailureException("Too many transient errors (" + numAttempts + ") for method ["
                + pjp.getSignature().toLongString() + "]. Giving up!");
    }

    private void handleTransientException(Throwable ex, int numAttempts, int totalAttempts,
            ProceedingJoinPoint pjp, AtomicLong backoffMillis) {
        // 记录重试日志（warn 级别），包含当前尝试次数、待重试等待时间和调用方法信息
        if (logger.isWarnEnabled()) {
            logger.warn("Transient data access exception (" + numAttempts + " of max " + totalAttempts + ") "
                    + "detected (retry in " + backoffMillis + " ms) "
                    + "in method '" + pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName()
                    + "': " + ex.getMessage());
        }
        if (backoffMillis.get() >= 0) {
            try {
                // 线程睡眠做退避（backoff）以降低短期内的重试冲突
                Thread.sleep(backoffMillis.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 逐步增加退避时间（不超过 1500 ms）
            backoffMillis.set(Math.min((long) (backoffMillis.get() * 1.5), 1500));
        }
    }
}