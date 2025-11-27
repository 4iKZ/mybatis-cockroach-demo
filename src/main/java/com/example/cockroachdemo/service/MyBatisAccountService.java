package com.example.cockroachdemo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.example.cockroachdemo.batchmapper.BatchAccountMapper;
import com.example.cockroachdemo.mapper.AccountMapper;
import com.example.cockroachdemo.model.Account;
import com.example.cockroachdemo.model.BatchResults;

import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
/**
 * AccountService 的 MyBatis 实现：
 * - 通过注入的 Mapper（mapper, batchMapper）与数据库交互
 * - 许多方法使用 REQUIRES_NEW 的事务传播，以模拟每个独立业务操作都有自己的事务边界（演示目的）
 * - 批量操作使用 BatchAccountMapper 并通过 flush() 触发 SQL 执行
 */
public class MyBatisAccountService implements AccountService {
    @Autowired
    private AccountMapper mapper;
    @Autowired
    private BatchAccountMapper batchMapper;
    private Random random = new Random();

    @Override
    /**
     * 创建 accounts 表（如果不存在）。
     * 使用 REQUIRES_NEW 事务传播级别：每次调用都会在新的事务中执行，这样可使演示中的事务隔离更明显。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAccountsTable() {
        mapper.createAccountsTable();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResults addAccounts(Account... accounts) {
        /**
         * 使用 BatchAccountMapper 将多个账户作为一个批次插入：
         * - insertAccount(account) 只是将 SQL 放入当前会话的批队列
         * - 调用 flush() 会真正发送并执行该批次 SQL，返回 BatchResult 列表
         */
        for (Account account : accounts) {
            batchMapper.insertAccount(account);
        }
        List<BatchResult> results = batchMapper.flush();

        // 本方法总是使用单个批次提交（演示简化），因此 numberOfBatches 固定为 1
        return new BatchResults(1, calculateRowsAffectedBySingleBatch(results));
    }

    private int calculateRowsAffectedBySingleBatch(List<BatchResult> results) {
        return results.stream()
                .map(BatchResult::getUpdateCounts)
                .flatMapToInt(Arrays::stream)
                .sum();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResults bulkInsertRandomAccountData(int numberToInsert) {
        List<List<BatchResult>> results = new ArrayList<>();

        // 随机生成 account id 和 balance（示例目的）。注意：随机 id 可能重复导致 upsert 更新而不是插入。
        for (int i = 0; i < numberToInsert; i++) {
            Account account = new Account();
            account.setId(random.nextInt(1000000000));
            account.setBalance(random.nextInt(1000000000));
            batchMapper.insertAccount(account);
        }

        // 将所有积累的批次一次 flush
        results.add(batchMapper.flush());

        return new BatchResults(results.size(), calculateRowsAffectedByMultipleBatches(results));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResults bulkInsertRandomAccountData(int numberToInsert, int batchSize) {
        List<List<BatchResult>> results = new ArrayList<>();

        // 支持按 batchSize 分段提交批次：每个批次满后触发一次 flush
        for (int i = 0; i < numberToInsert; i++) {
            Account account = new Account();
            account.setId(random.nextInt(1000000000));
            account.setBalance(random.nextInt(1000000000));
            batchMapper.insertAccount(account);
            if ((i + 1) % batchSize == 0) {
                // 到达 batchSize 大小后，flush 当前批次并记录结果
                results.add(batchMapper.flush());
            }
        }
        if (numberToInsert % batchSize != 0) {
            results.add(batchMapper.flush());
        }
        return new BatchResults(results.size(), calculateRowsAffectedByMultipleBatches(results));
    }

    private int calculateRowsAffectedByMultipleBatches(List<List<BatchResult>> results) {
        return results.stream()
                .mapToInt(this::calculateRowsAffectedBySingleBatch)
                .sum();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Account> getAccount(int id) {
        return mapper.findAccountById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int transferFunds(int fromId, int toId, int amount) {
        return mapper.transfer(fromId, toId, amount);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long findCountOfAccounts() {
        return mapper.findCountOfAccounts();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteAllAccounts() {
        return mapper.deleteAllAccounts();
    }
}
