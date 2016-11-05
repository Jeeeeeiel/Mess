package com.jeiel.bean;

public class Drama {
	private String name;
	private String se;
	private String url;
	
	public Drama() {
		super();
		this.name = "";
		this.se = "";
		this.url = "";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSe() {
		return se;
	}
	public void setSe(String se) {
		this.se = se;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
