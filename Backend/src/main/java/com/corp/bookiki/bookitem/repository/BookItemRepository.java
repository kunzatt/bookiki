package com.corp.bookiki.bookitem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookitem.entity.BookItemEntity;

@Repository
public interface BookItemRepository extends JpaRepository<BookItemEntity, Integer> {
	Page<BookItemEntity> findByDeletedFalse(Pageable pageable);
}