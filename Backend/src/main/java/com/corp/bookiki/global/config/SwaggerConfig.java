package com.corp.bookiki.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Bookiki API Documentation")
				.description("<h3>Bookiki Reference for Developers</h3>Swagger를 이용한 Bookiki API")
				.version("v1.0")
				.contact(new Contact()
					.name("Support Team")
					.email("support@bookiki.com")
					.url("https://github.com/bookiki"))
				.license(new License()
					.name("Bookiki License")
					.url("https://github.com/bookiki")))
			.externalDocs(new ExternalDocumentation()
				.description("Bookiki Documentation")
				.url("https://github.com/bookiki"))
			.servers(List.of(
				new Server().url("/api").description("Local server")
			));
	}
}
