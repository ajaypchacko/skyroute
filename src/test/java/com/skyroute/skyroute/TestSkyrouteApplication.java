package com.skyroute.skyroute;

import org.springframework.boot.SpringApplication;

public class TestSkyrouteApplication {

	public static void main(String[] args) {
		SpringApplication.from(SkyrouteApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
