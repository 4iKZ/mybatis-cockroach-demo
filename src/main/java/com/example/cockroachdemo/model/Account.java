package com.example.cockroachdemo.model;

/**
 * 账户模型（POJO）：
 * - id: 账户主键，整型
 * - balance: 账户余额，整型（示例项目中使用整数表示金额，以简化演示）
 * 
 * 说明：在真实应用中可能需要使用更合适的数据类型（如 BigDecimal）来表示金额，
 * 并且需要更多字段（如货币类型、时间戳、版本号等）以支持并发和业务需求。
 */
public class Account {
    /** 账户 ID（主键） */
    private int id;
    /** 账户余额（示例中使用 int 表示金额，实际应用中推荐使用 BigDecimal） */
    private int balance;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
