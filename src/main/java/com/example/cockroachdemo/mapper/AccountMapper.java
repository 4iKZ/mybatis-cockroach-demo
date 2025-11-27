package com.example.cockroachdemo.mapper;

import java.util.List;
import java.util.Optional;

import com.example.cockroachdemo.model.Account;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {
    /**
     * 删除 accounts 表中所有记录。请谨慎调用（演示使用）。
     * 
     * @return 被删除的行数
     */
    @Delete("delete from accounts")
    int deleteAllAccounts();

    /**
     * 更新单个账户的余额。
     * 注意：SQL 中使用了 ${id} 字面量而不是参数绑定，这里基于演示场景并且 id 是整数，不建议在用户输入中使用此模式以避免 SQL 注入风险。
     * 
     * @param account 传入包含 id 和新的 balance 的 Account 对象
     */
    @Update("update accounts set balance=#{balance} where id=${id}")
    void updateAccount(Account account);

    /**
     * 根据 id 查询单个账户，返回 Optional（找不到时为空）。
     */
    @Select("select id, balance from accounts where id=#{id}")
    Optional<Account> findAccountById(int id);

    /**
     * 查询所有账户并按 id 排序。
     */
    @Select("select id, balance from accounts order by id")
    List<Account> findAllAccounts();

    /**
     * 转账操作（使用 upsert）：
     * - 从 fromId 的账户中扣除 amount
     * - 向 toId 的账户中增加 amount
     * - 使用 upsert 可以在目标不存在时进行插入（演示使用），真实系统中请根据业务需求做更严格的校验
     * 
     * @return 受影响的行数（通常为 2，即两个账户的更新/插入）
     */
    @Update({
            "upsert into accounts (id, balance) values",
            "(#{fromId}, ((select balance from accounts where id = #{fromId}) - #{amount})),",
            "(#{toId}, ((select balance from accounts where id = #{toId}) + #{amount}))",
    })
    int transfer(@Param("fromId") int fromId, @Param("toId") int toId, @Param("amount") int amount);

    /**
     * 创建 accounts 表（如果不存在）。
     * 表定义示例：id 主键，balance 非负约束（CHECK）。
     * 注意：这只是演示用的简单建表语句，在生产环境中请使用更严格的 schema 设计和迁移工具（如 Flyway/Liquibase）。
     */
    @Update("CREATE TABLE IF NOT EXISTS accounts (id INT PRIMARY KEY, balance INT, CONSTRAINT balance_gt_0 CHECK (balance >= 0))")
    void createAccountsTable();

    /**
     * 统计 accounts 表中行数。
     * 
     * @return 账户数量
     */
    @Select("select count(*) from accounts")
    Long findCountOfAccounts();
}
