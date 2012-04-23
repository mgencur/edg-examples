package com.jboss.datagrid.tweetquick.session;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DisplayTweet {

	private static final long serialVersionUID = 29993133022854381L;
	private static final long MS_PER_SECOND = 1000;
	private static final long MS_PER_MINUTE = 60 * MS_PER_SECOND;
	private static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
	private static final long MS_PER_DAY = 24 * MS_PER_HOUR;
	private static final SimpleDateFormat df = new SimpleDateFormat("d MMM");

	private String name;
	private String username;
	private String message;
	private long timeOfPost;

	public DisplayTweet(String name, String username, String message,
			long timeOfPost) {
		this.name = name;
		this.username = username;
		this.message = message;
		this.timeOfPost = timeOfPost;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getMessage() {
		return message;
	}

	public String getFriendlyTimeOfPost() {
		Date now = Calendar.getInstance().getTime();

		long age = now.getTime() - getTimeOfPost();

		long days = (long) Math.floor(age / MS_PER_DAY);
		age -= (days * MS_PER_DAY);
		long hours = (long) Math.floor(age / MS_PER_HOUR);
		age -= (hours * MS_PER_HOUR);
		long minutes = (long) Math.floor(age / MS_PER_MINUTE);

		if (days < 7) {
			StringBuilder sb = new StringBuilder();

			if (days > 0) {
				sb.append(days);
				sb.append(days > 1 ? " days " : " day ");
			}

			if (hours > 0) {
				sb.append(hours);
				sb.append(hours > 1 ? " hrs " : " hr ");
			}

			if (minutes > 0) {
				sb.append(minutes);
				sb.append(minutes > 1 ? " minutes " : " minute ");
			}

			if (hours == 0 && minutes == 0) {
				sb.append("just now");
			} else {
				sb.append("ago");
			}

			return sb.toString();
		} else {
			return df.format(getTimeOfPost());
		}
	}

	public long getTimeOfPost() {
		return timeOfPost;
	}
}
