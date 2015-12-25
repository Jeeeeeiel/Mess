package com.jeiel.tool;

public class PDF {
	private String name;
	private String time;
	private String url;
	private String fileName;
	private boolean distributed = false;
	private boolean downloaded = false;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean isDistributed() {
		return distributed;
	}
	public void setDistributed(boolean distributed) {
		this.distributed = distributed;
	}
	public boolean isDownloaded() {
		return downloaded;
	}
	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}
	
}
