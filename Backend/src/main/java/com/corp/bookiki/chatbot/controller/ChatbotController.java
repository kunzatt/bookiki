package com.corp.bookiki.chatbot.controller;

import com.corp.bookiki.chatbot.dto.ChatbotRequest;
import com.corp.bookiki.chatbot.dto.ChatbotResponse;
import com.corp.bookiki.chatbot.service.ChatbotService;
import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Tag(name = "챗봇 API", description = "챗봇 관련 API")
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(
            summary = "챗봇 메시지 전송",
            description = "사용자의 메시지를 처리하고 챗봇의 응답을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메시지 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatbotResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/message")
    public ResponseEntity<ChatbotResponse> sendMessage(
            @Parameter(hidden = true) @CurrentUser AuthUser user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ChatbotRequest.class)
                    )
            )
            @Valid @RequestBody ChatbotRequest request
    ) {
        log.info("챗봇 메시지 수신 - 사용자: {}, 메시지: {}", user.getId(), request.getMessage());
        ChatbotResponse response = chatbotService.processMessage(request, user);
        return ResponseEntity.ok(response);
    }
}