package com.corp.bookiki.bookhistory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookRankingResponse {

	private Integer bookItemId;
	private String title;
	private String author;
	private Integer category;
	private String image;
	private Long borrowCount;

}
