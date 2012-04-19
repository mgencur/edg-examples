package com.jboss.datagrid.tweetquick.model;

import java.io.Serializable;
import java.util.Calendar;

public class Tweet implements Serializable {
	
	private static final long serialVersionUID = 29993133022854381L;
	
	private TweetKey key;
	private String message;
	private String owner;
	private long timeOfPost;

	public Tweet(String username, String message) {
		this.message = message;
		this.owner = username;
		this.timeOfPost = Calendar.getInstance().getTimeInMillis();
		this.key = new TweetKey(username, timeOfPost);
	}
	
	public TweetKey getKey() {
		return key;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public long getTimeOfPost() {
		return timeOfPost;
	}

	public void setTimeOfPost(long timeOfPost) {
		this.timeOfPost = timeOfPost;
	}

}
