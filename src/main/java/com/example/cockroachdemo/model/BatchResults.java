package com.example.cockroachdemo.model;

/**
 * 批处理结果模型：
 * - numberOfBatches: 批次数量
 * - totalRowsAffected: 批处理影响的总行数（通常是批次中 update/insert 的总和）
 */
public class BatchResults {
    /** 批次数量 */
    private int numberOfBatches;
    /** 批处理影响的总行数 */
    private int totalRowsAffected;

    public BatchResults(int numberOfBatches, int totalRowsAffected) {
        this.numberOfBatches = numberOfBatches;
        this.totalRowsAffected = totalRowsAffected;
    }

    public int getNumberOfBatches() {
        return numberOfBatches;
    }

    public int getTotalRowsAffected() {
        return totalRowsAffected;
    }
}