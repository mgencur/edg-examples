package com.jboss.datagrid.tweetquick.model;

public class TweetKey {

   private String owner;
   private long timeOfPost;

   public TweetKey(String owner, long timeOfPost) {
      this.owner = owner;
      this.timeOfPost = timeOfPost;
   }

   public String getOwner() {
      return owner;
   }

   public void setOwner(String username) {
      this.owner = username;
   }

   public long getTimeOfPost() {
      return timeOfPost;
   }

   public void setTimeOfPost(long timeOfPost) {
      this.timeOfPost = timeOfPost;
   }

}
