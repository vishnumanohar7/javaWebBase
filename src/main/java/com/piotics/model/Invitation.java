package com.piotics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "invitation")
public class Invitation {

	@Id
	private String id;
	private String email;
	private String phone;
	private String invitedBY;
	private Token token;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getInvitedBY() {
		return invitedBY;
	}
	public void setInvitedBY(String invitedBY) {
		this.invitedBY = invitedBY;
	}
	public Token getToken() {
		return token;
	}
	public void setToken(Token token) {
		this.token = token;
	}
	
	
}