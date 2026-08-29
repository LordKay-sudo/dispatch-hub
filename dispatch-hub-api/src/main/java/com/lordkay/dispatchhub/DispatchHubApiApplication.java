package com.lordkay.dispatchhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
		"com.lordkay.dispatchhub.tenant",
		"com.lordkay.dispatchhub.user",
		"com.lordkay.dispatchhub.destination",
		"com.lordkay.dispatchhub.event",
		"com.lordkay.dispatchhub.delivery",
		"com.lordkay.dispatchhub.security"
})
@EnableJdbcRepositories(basePackages = "com.lordkay.dispatchhub.dispatch")
public class DispatchHubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DispatchHubApiApplication.class, args);
	}

}
