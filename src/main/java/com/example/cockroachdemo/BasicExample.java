package com.example.cockroachdemo;

import java.time.LocalTime;

import com.example.cockroachdemo.model.Account;
import com.example.cockroachdemo.model.BatchResults;
import com.example.cockroachdemo.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
/**
 * 基本示例类（CommandLineRunner）：
 * 在 Spring Boot 启动后执行演示逻辑，演示了如何使用 AccountService 对账户表进行创建、删除、插入、查询、转账和批量插入等操作。
 * 该类仅在非 test profile 下运行（由 @Profile("!test") 控制），用于 CLI 演示而非单元测试。
 */
public class BasicExample implements CommandLineRunner {
    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        // 启动后首先确保数据库中有 accounts 表（如果不存在则创建）
        accountService.createAccountsTable();
        deleteAllAccounts();
        insertAccounts();
        printNumberOfAccounts();
        printBalances();
        transferFunds();
        printBalances();
        bulkInsertRandomAccountData();
        printNumberOfAccounts();
    }

    private void deleteAllAccounts() {
        // 删除所有账户（谨慎操作）并打印被删除的记录数
        int numDeleted = accountService.deleteAllAccounts();
        System.out.printf("deleteAllAccounts:\n    => %s total deleted accounts\n", numDeleted);
    }

    private void insertAccounts() {
        Account account1 = new Account();
        account1.setId(1);
        account1.setBalance(1000);

        // 创建并插入两个示例账户（id=1, id=2），用于后续的转账演示
        Account account2 = new Account();
        account2.setId(2);
        account2.setBalance(250);
        BatchResults results = accountService.addAccounts(account1, account2);
        System.out.printf("insertAccounts:\n    => %s total new accounts in %s batches\n",
                results.getTotalRowsAffected(), results.getNumberOfBatches());
    }

    private void printBalances() {
        int balance1 = accountService.getAccount(1).map(Account::getBalance).orElse(-1);
        int balance2 = accountService.getAccount(2).map(Account::getBalance).orElse(-1);

        // 输出两个账户的当前余额
        System.out.printf("printBalances:\n    => Account balances at time '%s':\n    ID %s => $%s\n    ID %s => $%s\n",
                LocalTime.now(), 1, balance1, 2, balance2);
    }

    private void printNumberOfAccounts() {
        // 打印账户总数
        System.out.printf("printNumberOfAccounts:\n    => Number of accounts at time '%s':\n    => %s total accounts\n",
                LocalTime.now(), accountService.findCountOfAccounts());
    }

    private void transferFunds() {
        int fromAccount = 1;
        int toAccount = 2;
        int transferAmount = 100;
        int transferredAccounts = accountService.transferFunds(fromAccount, toAccount, transferAmount);
        // 执行转账操作并打印受影响的行数（转账为 upsert 实现，实际更新两条记录）
        System.out.printf("transferFunds:\n    => $%s transferred between accounts %s and %s, %s rows updated\n",
                transferAmount, fromAccount, toAccount, transferredAccounts);
    }

    private void bulkInsertRandomAccountData() {
        BatchResults results = accountService.bulkInsertRandomAccountData(500);
        // 批量插入 500 条随机生成的账户数据（用于演示批处理及性能）
        System.out.printf("bulkInsertRandomAccountData:\n    => finished, %s total rows inserted in %s batches\n",
                results.getTotalRowsAffected(), results.getNumberOfBatches());
    }
}
