package com.corp.bookiki.common.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ImageFileServiceTest {

    @Mock
    private File mockFile;

    @InjectMocks
    private ImageFileService imageFileService;

    private final String TEST_STORAGE_PATH = "/test/storage/path";
    private final String TEST_DIRECTORY_PATH = "/images/test/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageFileService, "storagePath", TEST_STORAGE_PATH);
    }

    @Test
    @DisplayName("파일 크기 초과 시 예외 발생")
    void uploadFile_WhenFileSizeExceeded_ThenThrowException() {
        // given
        log.info("큰 파일 업로드 테스트 시작");
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );
        log.info("큰 테스트 파일 생성 완료: {}bytes", largeFile.getSize());

        // when & then
        assertThatThrownBy(() -> imageFileService.uploadFile(largeFile, TEST_DIRECTORY_PATH))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_SIZE_EXCEEDED);
        log.info("파일 크기 초과 예외 테스트 완료");
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생")
    void uploadFile_WhenEmptyFile_ThenThrowException() {
        // given
        log.info("빈 파일 업로드 테스트 시작");
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
        log.info("빈 테스트 파일 생성 완료");

        // when & then
        assertThatThrownBy(() -> imageFileService.uploadFile(emptyFile, TEST_DIRECTORY_PATH))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
        log.info("빈 파일 업로드 예외 테스트 완료");
    }

    @Test
    @DisplayName("허용되지 않는 파일 형식 업로드 시 예외 발생")
    void uploadFile_WhenInvalidFileFormat_ThenThrowException() {
        // given
        log.info("잘못된 파일 형식 업로드 테스트 시작");
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );
        log.info("잘못된 형식의 테스트 파일 생성 완료");

        // when & then
        assertThatThrownBy(() -> imageFileService.uploadFile(invalidFile, TEST_DIRECTORY_PATH))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_INVALID_FORMAT);
        log.info("잘못된 파일 형식 업로드 예외 테스트 완료");
    }
}