package com.corp.bookiki.shelf;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ShelfException;
import com.corp.bookiki.shelf.dto.ShelfCreateRequest;
import com.corp.bookiki.shelf.dto.ShelfResponse;
import com.corp.bookiki.shelf.entity.ShelfEntity;
import com.corp.bookiki.shelf.repository.ShelfRepository;
import com.corp.bookiki.shelf.service.ShelfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelfServiceTest {

    @Mock
    private ShelfRepository shelfRepository;

    @InjectMocks
    private ShelfService shelfService;

    @Test
    @DisplayName("전체 책장 조회 성공")
    void selectAllShelf_Success() {
        // given
        ShelfEntity shelf1 = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(100)
                .build();
        ShelfEntity shelf2 = ShelfEntity.builder()
                .shelfNumber(2)
                .lineNumber(3)
                .category(200)
                .build();

        when(shelfRepository.findAll()).thenReturn(Arrays.asList(shelf1, shelf2));

        // when
        List<ShelfResponse> responses = shelfService.selectAllShelf();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getShelfNumber()).isEqualTo(1);
        assertThat(responses.get(0).getCategory()).isEqualTo(100);
    }

    @Test
    @DisplayName("잘못된 책장 번호로 생성 실패")
    void createShelf_WithInvalidShelfNumber_ThrowsException() {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(0);
        request.setLineNumber(2);
        request.setCategory(100);

        // when & then
        assertThatThrownBy(() -> shelfService.createShelf(request))
                .isInstanceOf(ShelfException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SHELF_NUMBER);

        // repository의 save 메서드가 호출되지 않았는지 검증
        verify(shelfRepository, never()).save(any(ShelfEntity.class));
    }

    @Test
    @DisplayName("잘못된 라인 번호로 생성 실패")
    void createShelf_WithInvalidLineNumber_ThrowsException() {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(1);
        request.setLineNumber(0);
        request.setCategory(100);

        // when & then
        assertThatThrownBy(() -> shelfService.createShelf(request))
                .isInstanceOf(ShelfException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_LINE_NUMBER);

        verify(shelfRepository, never()).save(any(ShelfEntity.class));
    }

    @Test
    @DisplayName("책장 생성 성공")
    void createShelf_Success() {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(1);
        request.setLineNumber(2);
        request.setCategory(100);

        ShelfEntity savedShelf = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(100)
                .build();

        ReflectionTestUtils.setField(savedShelf, "id", 1);

        when(shelfRepository.save(any(ShelfEntity.class))).thenReturn(savedShelf);

        // when
        int id = shelfService.createShelf(request);

        // then
        assertThat(id).isEqualTo(1);
        verify(shelfRepository).save(any(ShelfEntity.class));
    }
}
