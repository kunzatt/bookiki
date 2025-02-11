package com.corp.bookiki.user.controller;

import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.service.ProfileImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/{userId}/profile-image")
@RequiredArgsConstructor
@Tag(name = "프로필 이미지 관리 API", description = "프로필 이미지 수정, 삭제, 조회 API")
@Slf4j
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @Operation(
            summary = "프로필 사진 변경",
            description = "업로드한 사진으로 프로필 사진을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 사진 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(value = "")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파일 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                               {
                                   "code": "FILE_INVALID_FORMAT",
                                   "message": "지원하지 않는 파일 형식입니다."
                               }
                               """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                               {
                                   "code": "USER_NOT_FOUND",
                                   "message": "사용자를 찾을 수 없습니다."
                               }
                               """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "파일 크기 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                               {
                                   "code": "FILE_SIZE_EXCEEDED",
                                   "message": "파일 크기가 제한을 초과했습니다."
                               }
                               """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "파일 업로드/삭제 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                               {
                                   "code": "FILE_UPLOAD_FAILED",
                                   "message": "파일 업로드에 실패했습니다."
                               }
                               """
                            )
                    )
            )
    })
    @PutMapping
    public ResponseEntity<?> updateProfileImage(@RequestParam("file") MultipartFile file,
                                                @CurrentUser AuthUser authUser){
        Integer userId = authUser.getId();
        log.info("유저 이미지 변경 시작 - 유저 ID: {}", userId);
        profileImageService.updateProfileImage(userId, file);
        log.info("유저 이미지 변경 완료 - 유저 ID: {}", userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "프로필 사진 삭제",
            description = "프로필 사진을 기본 이미지로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "프로필 사진 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "code": "USER_NOT_FOUND",
                                    "message": "사용자를 찾을 수 없습니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "파일 삭제 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "code": "FILE_DELETE_FAILED",
                                    "message": "파일 삭제에 실패했습니다."
                                }
                                """
                            )
                    )
            )
    })
    @DeleteMapping
    public ResponseEntity<?> deleteProfileImage(@CurrentUser AuthUser authUser){
        Integer userId = authUser.getId();
        log.info("유저 이미지 삭제 시작 - 유저 ID: {}", userId);
        profileImageService.deleteProfileImage(userId);
        log.info("유저 이미지 삭제 완료 - 유저 ID: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "프로필 사진 조회",
            description = "사용자의 프로필 사진 URL을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 사진 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "imageUrl": "/images/profile/example.jpg"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "code": "USER_NOT_FOUND",
                                    "message": "사용자를 찾을 수 없습니다."
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<?> getProfileImage(@CurrentUser AuthUser authUser){
        Integer userId = authUser.getId();
        log.info("유저 프로필 이미지 조회 시작 - 유저 ID: {}", userId);
        ProfileResponse response = profileImageService.getProfileImage(userId);
        log.info("유저 프로필 이미지 조회 완료 - 유저 ID: {}", userId);
        return ResponseEntity.ok(response);
    }
}
