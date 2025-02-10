package com.corp.bookiki.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Bookiki API Documentation")
				.description("<h3>Bookiki Reference for Developers </h3>Swagger를 이용한 Bookiki API")
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
				new Server().url("").description("Local server")
			))
			.components(new Components()
				.addSecuritySchemes("bearerAuth",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER)))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}

	/**
	 * Swagger에서 @CurrentUser AuthUser authUser를 자동으로 제거하여 JSON 입력 없이 Bearer Token을 기반으로 인증 가능하게 설정
	 */
	@Bean
	public OperationCustomizer customizeOperation() {
		return (operation, handlerMethod) -> {
			List<Parameter> parameters = operation.getParameters();
			if (parameters != null) {
				parameters.removeIf(param -> param.getName().equals("authUser"));
			}
			return operation;
		};
	}
}
