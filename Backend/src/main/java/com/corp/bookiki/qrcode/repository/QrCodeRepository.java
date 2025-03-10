package com.corp.bookiki.qrcode.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.qrcode.entity.QrCodeEntity;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCodeEntity, Integer> {
	Optional<QrCodeEntity> findByBookItemId(Integer bookItemId);
}