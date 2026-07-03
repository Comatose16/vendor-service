package com.forthestreets;

import org.springframework.boot.SpringApplication;

public class TestFtsApplication {

	public static void main(String[] args) {
		SpringApplication.from(VendorServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
