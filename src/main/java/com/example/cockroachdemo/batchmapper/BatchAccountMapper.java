package com.example.cockroachdemo.batchmapper;

import java.util.List;

import com.example.cockroachdemo.model.Account;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.executor.BatchResult;

/**
 * 批量插入 Mapper:
 * - 通过在 Mapper 上使用批量执行器 (SqlSessionTemplate with ExecutorType.BATCH) 来收集多条 SQL
 * 并在调用 flush 时一起提交。
 * - 使用 upsert 插入/更新账户（演示目的）。
 */
@Mapper
public interface BatchAccountMapper {
    /**
     * 将一条账户数据添加到当前批次（不立即提交到数据库）。
     * 批次内的 SQL 在调用 flush() 时统一发送到数据库执行。
     */
    @Insert("upsert into accounts(id, balance) values(#{id}, #{balance})")
    void insertAccount(Account account);

    /**
     * 将当前批次中的所有 SQL 提交并返回执行结果的详情（BatchResult 列表）。
     * 该方法由 MyBatis 的 @Flush 注解标注，用于在批量操作中显式触发批提交。
     */
    @Flush
    List<BatchResult> flush();
}
