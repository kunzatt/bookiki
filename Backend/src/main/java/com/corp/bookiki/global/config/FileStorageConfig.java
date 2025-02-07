package com.corp.bookiki.global.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FileStorageConfig {
	@Value("${spring.file.upload.path}")
	private String uploadPath;

	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(Paths.get(uploadPath));
		} catch (IOException e) {
			throw new RuntimeException("Could not create upload directory!");
		}
	}
}