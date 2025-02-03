package com.corp.bookiki.shelf.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ShelfException;
import com.corp.bookiki.shelf.dto.ShelfCreateRequest;
import com.corp.bookiki.shelf.dto.ShelfResponse;
import com.corp.bookiki.shelf.dto.ShelfUpdateRequest;
import com.corp.bookiki.shelf.entity.ShelfEntity;
import com.corp.bookiki.shelf.repository.ShelfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShelfService {

    private final ShelfRepository shelfRepository;

    // 책장 정보 전체 조회
    public List<ShelfResponse> selectAllShelf(){
        try{
            List<ShelfEntity> list = shelfRepository.findAll();

            return list.stream()
                    .map(ShelfResponse::from)  // Entity를 Response DTO로 변환
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("책장 정보 조회 중 예상치 못한 오류 발생: {}", ex.getMessage());
            throw new ShelfException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 책장 정보 저장
    @Transactional
    public int createShelf(ShelfCreateRequest request) {
        try{
            if (request.getShelfNumber() <= 0) {
                throw new ShelfException(ErrorCode.INVALID_SHELF_NUMBER);
            }
            if (request.getLineNumber() <= 0) {
                throw new ShelfException(ErrorCode.INVALID_LINE_NUMBER);
            }

            ShelfEntity shelfEntity = shelfRepository.save(request.toEntity());
            return shelfEntity.getId();
        } catch (Exception ex) {
            log.error("책장 정보 등록 실패: {}", ex.getMessage());
            throw new ShelfException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 책장 정보 수정
    @Transactional
    public void updateShelf(ShelfUpdateRequest request) {
        try{
            ShelfEntity shelfEntity = shelfRepository.findById(request.getId())
                    .orElseThrow(() -> new ShelfException(ErrorCode.SHELF_NOT_FOUND));
            if (request.getShelfNumber() <= 0) {
                throw new ShelfException(ErrorCode.INVALID_SHELF_NUMBER);
            }
            if (request.getLineNumber() <= 0) {
                throw new ShelfException(ErrorCode.INVALID_LINE_NUMBER);
            }

            shelfEntity.setShelfNumber(request.getShelfNumber());
            shelfEntity.setLineNumber(request.getLineNumber());
            shelfEntity.setCategory(request.getCategory());

            shelfRepository.save(shelfEntity);
        } catch (ShelfException ex) {
            log.error("해당 id의 책장을 찾을 수 없음", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("책장 정보 수정 실패: {}", ex.getMessage());
            throw new ShelfException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 책장 삭제
    @Transactional
    public void deleteShelf(int id) {
        try{
            ShelfEntity shelfEntity = shelfRepository.findById(id)
                    .orElseThrow(() -> new ShelfException(ErrorCode.SHELF_NOT_FOUND));
            shelfRepository.deleteById(id);
        } catch (ShelfException ex) {
            log.error("해당 id의 책장을 찾을 수 없음", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("책장 정보 삭제 실패: {}", ex.getMessage());
            throw new ShelfException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
