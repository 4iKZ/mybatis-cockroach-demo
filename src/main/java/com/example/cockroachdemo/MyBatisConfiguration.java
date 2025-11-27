package com.example.cockroachdemo;

import javax.sql.DataSource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MyBatis 配置类：
 * - 为演示目的在默认 Mapper 中使用普通 SqlSessionTemplate（默认 ExecutorType）
 * - 为批量插入操作配置了一个使用 BATCH Executor 的 `batchSqlSessionTemplate`
 * 这允许部分 Mapper（位于 `com.example.cockroachdemo.batchmapper`
 * 包）使用批处理执行器来更高效地执行大量插入操作。
 * 
 * 说明：当使用 MyBatis Spring Boot Starter 时，只有在需要自定义
 * SqlSessionTemplate（如使用批处理）时才需要此类。
 */
@Configuration
@MapperScan(basePackages = "com.example.cockroachdemo.mapper", annotationClass = Mapper.class)
@MapperScan(basePackages = "com.example.cockroachdemo.batchmapper", annotationClass = Mapper.class, sqlSessionTemplateRef = "batchSqlSessionTemplate")
public class MyBatisConfiguration {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        return factory.getObject();
    }

    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        // 默认的 SqlSessionTemplate，用于大多数 Mapper（普通执行器模式）
        return new SqlSessionTemplate(sqlSessionFactory());
    }

    @Bean(name = "batchSqlSessionTemplate")
    public SqlSessionTemplate batchSqlSessionTemplate() throws Exception {
        // 为需要批量执行的 Mapper 提供一个使用 BATCH 执行器的 SqlSessionTemplate
        // 使用批处理可以在大量插入/更新场景下显著提升性能，但也需注意事务大小和内存消耗。
        return new SqlSessionTemplate(sqlSessionFactory(), ExecutorType.BATCH);
    }
}
