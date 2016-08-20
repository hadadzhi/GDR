package ru.cdfe.gdr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;

@SpringBootApplication
public class MongoGDRApplication {
	public static void main(String[] args) {
		SpringApplication.run(MongoGDRApplication.class, args);
	}
	
	@Bean
	public CurieProvider curieProvider(MongoGDRProperties conf) {
		return new DefaultCurieProvider(conf.getCurieName(), new UriTemplate(conf.getCurieUrlTemplate()));
	}
}

