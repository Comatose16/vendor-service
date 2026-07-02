package com.nicebreeze.fts;

import org.springframework.boot.SpringApplication;

public class TestFtsApplication {

	public static void main(String[] args) {
		SpringApplication.from(FtsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
