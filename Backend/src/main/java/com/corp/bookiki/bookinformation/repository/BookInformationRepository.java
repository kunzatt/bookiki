package com.corp.bookiki.bookinformation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;

@Repository
public interface BookInformationRepository extends JpaRepository<BookInformationEntity, Integer> {

	// Isbn을 이용한 책 정보 불러오기
	BookInformationEntity findByIsbn(String isbn);
}
