package com.corp.bookiki.bookhistory.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookReturnRequest {

	private List<Integer> scannedBookItemIds;
	private List<String> ocrResults;

}
