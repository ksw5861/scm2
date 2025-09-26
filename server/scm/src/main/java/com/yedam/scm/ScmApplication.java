package com.yedam.scm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan(basePackages = "com.yedam.scm.**.mapper")
@EnableAsync
public class ScmApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScmApplication.class, args);
	}

}
