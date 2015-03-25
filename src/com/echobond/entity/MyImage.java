package com.echobond.entity;

public class MyImage {
	private int id;
	private String path;
	private String thumbnailPath;
	private byte[] src;
	private byte[] thumbnailSrc;
	             
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public byte[] getImageSrc() {
		return src;
	}
	public void setImageSrc(byte[] src) {
		this.src = src;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getThumbnailPath() {
		return thumbnailPath;
	}
	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}
	public byte[] getSrc() {
		return src;
	}
	public void setSrc(byte[] src) {
		this.src = src;
	}
	public byte[] getThumbnailSrc() {
		return thumbnailSrc;
	}
	public void setThumbnailSrc(byte[] thumbnailSrc) {
		this.thumbnailSrc = thumbnailSrc;
	}
	
	
}
