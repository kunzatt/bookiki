package com.corp.bookiki.shelf.repository;

import com.corp.bookiki.shelf.entity.ShelfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelfRepository extends JpaRepository<ShelfEntity, Integer> {
    // findAll(), findById(), save(), delete()는 JpaRepository에서 기본 제공

}
