package com.corp.bookiki.shelf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.shelf.entity.ShelfEntity;

@Repository
public interface ShelfRepository extends JpaRepository<ShelfEntity, Integer> {
	Optional<ShelfEntity> findByCategory(Integer category);

}
