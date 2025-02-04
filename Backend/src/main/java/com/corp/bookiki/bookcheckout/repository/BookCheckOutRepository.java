package com.corp.bookiki.bookcheckout.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookcheckout.enitity.BookHistoryEntity;

@Repository
public interface BookCheckOutRepository extends JpaRepository<BookHistoryEntity, Integer> {

}
