package com.corp.bookiki.bookitem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookitem.entity.BookItemEntity;

@Repository
public interface BookItemRepository extends JpaRepository<BookItemEntity, Long> {
	Optional<BookItemEntity> findByQrCodeQrValue(String qrValue);
}