package com.hengyu.chapter39;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * quartz定时任务分布式节点-2
 * 程序启动入口
 * @author 恒宇少年
 */
@SpringBootApplication
public class Chapter49Application {

	/**
	 * logback
	 */
	private static Logger logger = LoggerFactory.getLogger(Chapter49Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Chapter49Application.class, args);

		logger.info("【【【【【【定时任务分布式节点 - quartz-cluster-node-49 已启动】】】】】】");
	}
}
