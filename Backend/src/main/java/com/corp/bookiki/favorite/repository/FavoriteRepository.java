package com.corp.bookiki.favorite.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.favorite.entity.FavoriteEntity;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Integer> {

	@Query("SELECT f FROM FavoriteEntity f " +
		"JOIN FETCH f.bookItem bi " +
		"JOIN FETCH bi.bookInformation " +
		"WHERE f.user.id = :userId")
	Page<FavoriteEntity> findByUserIdWithBookInformation(int userId, Pageable pageable);

	boolean existsByUserIdAndBookItemId(Integer userId, Integer bookItemId);

	int countByBookItemId(Integer bookItemId);

	void deleteByUserIdAndBookItemId(Integer userId, Integer bookItemId);

	int countByUserId(Integer userId);

	List<Integer> findUserIdByBookItemId(Integer bookItemId);

}
