package com.nimeshkadecha.myapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
	private int id;
	private String message;
	private boolean isSentByUser;
	private long timestamp;
	private int sellerId;

	public ChatMessage(String message, boolean isSentByUser, int sellerId) {
		this.message = message;
		this.isSentByUser = isSentByUser;
		this.timestamp = System.currentTimeMillis();
		this.sellerId = sellerId;
	}

	public ChatMessage(int id, String message, boolean isSentByUser, long timestamp, int sellerId) {
		this.id = id;
		this.message = message;
		this.isSentByUser = isSentByUser;
		this.timestamp = timestamp;
		this.sellerId = sellerId;
	}

	public ChatMessage(String message, boolean isSentByUser, int sellerId, String timestamp) {
		this.message = message;
		this.isSentByUser = isSentByUser;
		this.sellerId = sellerId;
		this.timestamp = convertTimestampToLong(timestamp);
	}

	private long convertTimestampToLong(String timestamp) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			Date date = dateFormat.parse(timestamp);
			return date != null ? date.getTime() : System.currentTimeMillis();
		} catch (ParseException e) {
			e.printStackTrace();
			return System.currentTimeMillis();
		}
	}

	public int getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSentByUser() {
		return isSentByUser;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getSellerId() {
		return sellerId;
	}
}
