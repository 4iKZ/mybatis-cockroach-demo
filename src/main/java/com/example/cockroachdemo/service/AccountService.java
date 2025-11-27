package com.example.cockroachdemo.service;

import java.util.Optional;

import com.example.cockroachdemo.model.Account;
import com.example.cockroachdemo.model.BatchResults;

/**
 * 服务接口：定义了账户相关的核心业务方法。实现类负责将这些方法映射到 MyBatis Mapper（数据库层）。
 */
public interface AccountService {
    /**
     * 创建 accounts 表（如果不存在）。业务层负责调用 Mapper 建表，通常只在初始化或演示时被使用。
     */
    void createAccountsTable();

    /**
     * 查询单个账户（返回 Optional）。
     * 
     * @param id 账户 id
     * @return Optional<Account>
     */
    Optional<Account> getAccount(int id);

    /**
     * 批量插入随机账户数据（使用默认的批量逻辑/分批大小）。
     * 
     * @param numberToInsert 要插入的数量
     * @return BatchResults 描述批次数量和受影响的总行数
     */
    BatchResults bulkInsertRandomAccountData(int numberToInsert);

    /**
     * 批量插入随机账户数据，并且指定每个批次的大小。
     * 
     * @param numberToInsert 要插入的总数
     * @param batchSize      每个批次的大小
     * @return BatchResults
     */
    BatchResults bulkInsertRandomAccountData(int numberToInsert, int batchSize);

    /**
     * 新增多个账户为一个批次并返回执行结果（用于演示单次批次）。
     */
    BatchResults addAccounts(Account... accounts);

    /**
     * 转账操作（从 fromAccount 减少 amount，并向 toAccount 增加 amount）。
     * 
     * @return 受影响的行数
     */
    int transferFunds(int fromAccount, int toAccount, int amount);

    /**
     * 统计账户数量
     */
    long findCountOfAccounts();

    /**
     * 删除 accounts 表中的所有记录（演示使用）。
     * 
     * @return 被删除的行数
     */
    int deleteAllAccounts();
}
