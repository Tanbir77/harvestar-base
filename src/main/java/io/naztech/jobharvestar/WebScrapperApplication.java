package io.naztech.jobharvestar;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class WebScrapperApplication extends SpringBootServletInitializer {

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(WebScrapperApplication.class, args);
	}

}
