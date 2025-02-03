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

// 스웨거 주소 : http://i12a206.p.ssafy.io:8088/api/swagger-ui/index.html#/

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
					.email("bookiki206@gmail.com")
					.url("https://lab.ssafy.com/s12-webmobile3-sub1/S12P11A206"))
				.license(new License()
					.name("Bookiki License")
					.url("https://lab.ssafy.com/s12-webmobile3-sub1/S12P11A206")))
			.externalDocs(new ExternalDocumentation()
				.description("Bookiki Documentation")
				.url("https://lab.ssafy.com/s12-webmobile3-sub1/S12P11A206"))
			.servers(List.of(
				new Server().url("/api").description("Local server")
			));
	}
}
