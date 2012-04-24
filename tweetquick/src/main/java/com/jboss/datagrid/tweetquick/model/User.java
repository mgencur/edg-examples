/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.jboss.datagrid.tweetquick.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * 
 * @author Martin Gencur
 * 
 */
public class User implements Serializable {

   private static final long serialVersionUID = 188164481825309731L;

   private String username; // unique identifier

   private String name;
   private String password;
   private String whoami; // description of the person
   private List<TweetKey> tweets;
   private List<String> watchers;
   private List<String> watching;

   public User(String username, String name, String surname, String password, String whoami) {
      this.username = username;
      this.name = name;
      this.password = password;
      this.whoami = whoami;
      this.tweets = new LinkedList<TweetKey>();
      this.watchers = new LinkedList<String>();
      this.watching = new LinkedList<String>();
   }

   public void addFollower(String user) {
      this.watchers.add(user);
   }

   public void removeFollower(String user) {
      this.watchers.remove(user);
   }

   public void addFollowing(String user) {
      this.watching.add(user);
   }

   public void removeFollowing(String user) {
      this.watching.remove(user);
   }

   public void addTweet(TweetKey tweet) {
      this.tweets.add(tweet);
   }

   // *************************************************************************************

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getWhoami() {
      return whoami;
   }

   public void setWhoami(String whoami) {
      this.whoami = whoami;
   }

   public List<TweetKey> getTweets() {
      return tweets;
   }

   public List<String> getWatchers() {
      return watchers;
   }

   public List<String> getWatching() {
      return watching;
   }

}