package com.jboss.datagrid.tweetquick.model;

import java.io.Serializable;
import java.util.Calendar;

public class Tweet implements Serializable {

   private static final long serialVersionUID = -3268797376991935010L;

   private TweetKey key;
   private String message;

   public Tweet(String username, String message) {
      this.message = message;
      this.key = new TweetKey(username, Calendar.getInstance().getTimeInMillis());
   }

   // for application initilization purposes
   public Tweet(String username, String message, long timestamp) {
      this.message = message;
      this.key = new TweetKey(username, timestamp);
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
      return this.key.getOwner();
   }

   public long getTimeOfPost() {
      return this.key.getTimeOfPost();
   }
}
