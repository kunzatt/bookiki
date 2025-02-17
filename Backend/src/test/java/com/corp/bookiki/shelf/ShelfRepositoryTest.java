package com.corp.bookiki.shelf;

import com.corp.bookiki.bookinformation.entity.Category;
import com.corp.bookiki.shelf.entity.ShelfEntity;
import com.corp.bookiki.shelf.repository.ShelfRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class ShelfRepositoryTest {

    @Autowired
    private ShelfRepository shelfRepository;

    @Test
    @DisplayName("책장 저장 성공")
    void save_Success() {
        // given
        ShelfEntity shelf = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(Category.COMPUTER_SCIENCE.getCode())
                .build();

        // when
        ShelfEntity savedShelf = shelfRepository.save(shelf);

        // then
        assertThat(savedShelf.getId()).isNotNull();
        assertThat(savedShelf.getShelfNumber()).isEqualTo(1);
        assertThat(savedShelf.getLineNumber()).isEqualTo(2);
        assertThat(savedShelf.getCategory()).isEqualTo(0);
    }

    @Test
    @DisplayName("ID로 책장 조회 성공")
    void findById_WhenExists_ThenReturnShelf() {
        // given
        ShelfEntity shelf = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(0)
                .build();

        ShelfEntity savedShelf = shelfRepository.save(shelf);

        // when
        Optional<ShelfEntity> found = shelfRepository.findById(savedShelf.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getShelfNumber()).isEqualTo(1);
        assertThat(found.get().getLineNumber()).isEqualTo(2);
        assertThat(found.get().getCategory()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회시 빈 Optional 반환")
    void findById_WhenNotExists_ThenReturnEmpty() {
        // when
        Optional<ShelfEntity> found = shelfRepository.findById(999);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("전체 책장 조회 성공")
    void findAll_Success() {
        // given
        ShelfEntity shelf1 = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(0)
                .build();

        ShelfEntity shelf2 = ShelfEntity.builder()
                .shelfNumber(2)
                .lineNumber(3)
                .category(1)
                .build();

        shelfRepository.save(shelf1);
        shelfRepository.save(shelf2);

        // when
        List<ShelfEntity> shelves = shelfRepository.findAll();

        // then
        assertThat(shelves).hasSize(2);
        assertThat(shelves).extracting("shelfNumber")
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("책장 정보 수정 성공")
    void update_Success() {
        // given
        ShelfEntity shelf = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(Category.COMPUTER_SCIENCE.getCode())
                .build();

        ShelfEntity savedShelf = shelfRepository.save(shelf);

        // when
        savedShelf.update(
                savedShelf.getShelfNumber(),  // 기존 값 유지
                savedShelf.getLineNumber(),   // 기존 값 유지
                Category.PHILOSOPHY_PSYCHOLOGY.getCode()    // 변경할 카테고리 값
        );

        ShelfEntity updatedShelf = shelfRepository.save(savedShelf);

        // then
        assertThat(updatedShelf.getCategory()).isEqualTo(1);
        assertThat(updatedShelf.getId()).isEqualTo(savedShelf.getId());
    }

    @Test
    @DisplayName("책장 삭제 성공")
    void delete_Success() {
        // given
        ShelfEntity shelf = ShelfEntity.builder()
                .shelfNumber(1)
                .lineNumber(2)
                .category(Category.COMPUTER_SCIENCE.getCode())
                .build();

        ShelfEntity savedShelf = shelfRepository.save(shelf);

        // when
        shelfRepository.deleteById(savedShelf.getId());

        // then
        Optional<ShelfEntity> found = shelfRepository.findById(savedShelf.getId());
        assertThat(found).isEmpty();
    }
}