package com.corp.bookiki.iot.service;

import java.nio.file.Files;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.corp.bookiki.common.service.ImageFileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IotStorageService {

	private static String savedImageString = "";  // 이미지 문자열을 저장할 static 변수

	public void saveImageString(String imageString) {
		savedImageString = imageString;
	}

	public String getImageString() {
		return savedImageString;
	}
}