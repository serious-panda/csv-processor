package com.dima.csvprocessor.model;

public class Review {
	public static Review EMPTY = new Review(-1); 
	
	private final long id;
	private String productId;
	private String userId;
	private String profileName;
	private String text;
	
	public Review(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {			
		return "[ id:" + id + ", product:" + productId + ", user:" + userId + ", text:" + text + "]";//text.substring(0, Math.min(15, text.length())) + "...]";
	}	
}
