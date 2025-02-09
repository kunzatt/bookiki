package com.corp.bookiki.iot.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.shelf.entity.ShelfEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IotMessage {
	private IotMessageType type;
	private String bookId;
	private ShelfInfo shelf;
	private LocalDateTime timestamp;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ShelfInfo {
		private Integer id;
		private Integer shelfNumber;
		private Integer lineNumber;
		private Integer category;

		public static ShelfInfo from(ShelfEntity entity) {
			ShelfInfo info = new ShelfInfo();
			info.setId(entity.getId());
			info.setShelfNumber(entity.getShelfNumber());
			info.setLineNumber(entity.getLineNumber());
			info.setCategory(entity.getCategory());
			return info;
		}
	}

	public static IotMessage bookReturn(String bookId) {
		IotMessage message = new IotMessage();
		message.setType(IotMessageType.BOOK_RETURN);
		message.setBookId(bookId);
		message.setTimestamp(LocalDateTime.now());
		return message;
	}

	public static IotMessage locationUpdate(String bookId, ShelfEntity shelf) {
		IotMessage message = new IotMessage();
		message.setType(IotMessageType.LOCATION_UPDATE);
		message.setBookId(bookId);
		message.setShelf(ShelfInfo.from(shelf));
		message.setTimestamp(LocalDateTime.now());
		return message;
	}

	public static IotMessage bookLocation(String bookId, ShelfEntity shelf) {
		IotMessage message = new IotMessage();
		message.setType(IotMessageType.BOOK_LOCATION);
		message.setBookId(bookId);
		message.setShelf(ShelfInfo.from(shelf));
		message.setTimestamp(LocalDateTime.now());
		return message;
	}
}