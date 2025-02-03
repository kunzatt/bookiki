package com.corp.bookiki.shelf.controller;

import com.corp.bookiki.shelf.dto.ShelfCreateRequest;
import com.corp.bookiki.shelf.dto.ShelfResponse;
import com.corp.bookiki.shelf.dto.ShelfUpdateRequest;
import com.corp.bookiki.shelf.service.ShelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/shelf/categories")
@RequiredArgsConstructor
@Tag(name = "책장 정보 API", description = "책장 정보 CRUD API")
@Slf4j
public class ShelfController {

    private final ShelfService shelfService;

    @GetMapping()
    @Operation(summary = "책장 전체 조회", description = "책장 정보를 전체 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "책장 전체 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShelfResponse.class, type = "array"),
                    examples = @ExampleObject(value = """
                            [
                              {
                                "id": 1,
                                "shelfNumber": 1,
                                "lineNumber": 3,
                                "category": "FICTION"
                              }
                            ]
                            """)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류가 발생했습니다",
            content = @Content
    )
    public ResponseEntity<?> selectAllShelf(){
        List<ShelfResponse> list = shelfService.selectAllShelf();
        log.info("전체 책장 정보 조회 - 책장 개수: {}", list.size());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Operation(summary = "책장 생성", description = "새로운 책장을 생성합니다.")
    @ApiResponse(
            responseCode = "201",
            description = "책장 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "integer"),
                    examples = @ExampleObject(value = "1")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                                "status": 400,
                                "message": "잘못된 책장 번호입니다"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류가 발생했습니다",
            content = @Content
    )
    public ResponseEntity<?> createShelf (@RequestBody ShelfCreateRequest request){
        log.info("책장 정보 등록: {}, {}, 카테고리: {}", request.getShelfNumber(), request.getLineNumber(), request.getCategory());
        int shelfId = shelfService.createShelf(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(shelfId);
    }

    @PutMapping
    @Operation(summary = "책장 수정", description = "기존 책장 정보를 수정합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "책장 수정 성공",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                                "status": 400,
                                "message": "잘못된 책장 번호입니다"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "책장을 찾을 수 없습니다",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류가 발생했습니다",
            content = @Content
    )
    public ResponseEntity<?> updateShelf (@RequestBody ShelfUpdateRequest request){
        log.info("책장 정보 수정: {}, {}, 카테고리: {}", request.getShelfNumber(), request.getLineNumber(), request.getCategory());
        shelfService.updateShelf(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/id")
    @Operation(summary = "책장 삭제", description = "지정된 책장을 삭제합니다.")
    @ApiResponse(
            responseCode = "204",
            description = "책장 삭제 성공",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "책장을 찾을 수 없습니다",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류가 발생했습니다",
            content = @Content
    )
    public ResponseEntity<?> deleteShelf (@PathVariable int id){
        log.info("책장 삭제 - id: {}", id);
        shelfService.deleteShelf(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
