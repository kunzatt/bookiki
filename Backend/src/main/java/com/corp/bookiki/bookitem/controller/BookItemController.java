package com.corp.bookiki.bookitem.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.service.BookItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books/search")
@RequiredArgsConstructor
public class BookItemController {

	private final BookItemService bookItemService;

	@GetMapping("/list")
	public Page<BookItemResponse> getAllBookItems(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "id") String sortBy,
		@RequestParam(defaultValue = "desc") String direction
	) {
		return bookItemService.getAllBookItems(page, size, sortBy, direction);
	}

	@GetMapping("/qrcodes/{id}")
	public BookItemResponse getBookItemById(@PathVariable Integer id) {
		return bookItemService.getBookItemById(id);
	}
}