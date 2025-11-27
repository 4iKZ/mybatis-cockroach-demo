package com.example.cockroachdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Spring Boot 应用程序入口：
 * 运行本类将启动 Spring Boot 上下文并执行任何实现了 CommandLineRunner 的组件（如 `BasicExample`）。
 */
public class CockroachDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(CockroachDemoApplication.class, args);
	}
}
