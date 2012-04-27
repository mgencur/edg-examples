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
package com.jboss.datagrid.tweetquick.session;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;
import org.infinispan.api.BasicCache;
import com.jboss.datagrid.tweetquick.model.Tweet;
import com.jboss.datagrid.tweetquick.model.TweetKey;
import com.jboss.datagrid.tweetquick.model.User;

/**
 * 
 * @author Martin Gencur
 * 
 */
@Named
@SessionScoped
public class TweetBean implements Serializable {

   private static final long serialVersionUID = -8914061755188086355L;

   private static final int RECENT_POSTS_LIMIT = 100;
   
   private static final long HOUR = 60 * 60 * 1000;
   
   private static final long TWELVE_HOURS = 12 * HOUR;
   
   private static final long DAY = 24 * HOUR;
   
   private static final long THREE_DAYS = DAY * 3;
   
   private static final long SEVEN_DAYS = DAY * 7;
   
   private static long ages[] = { HOUR, TWELVE_HOURS, DAY, THREE_DAYS, SEVEN_DAYS };

   private String message;

   BasicCache<String, Object> userCache;
   
   BasicCache<TweetKey, Object> tweetCache;

   @Inject
   private Instance<Authenticator> auth;

   @Inject
   private UserBean userBean;

   @Inject
   private CacheContainerProvider provider;

   @Inject
   private UserTransaction utx;
   
   public String sendTweet() {
      Tweet t = new Tweet(auth.get().getUsername(), message);
      try {
         utx.begin();
         // todo - is it necessary to retrieve it again and not use the one
         // in authenticator?
         User u = (User) getUserCache().get(auth.get().getUsername());
         getTweetCache().put(t.getKey(), t);
         u.getTweets().add(t.getKey());
         getUserCache().replace(auth.get().getUsername(), u);
         utx.commit();
      } catch (Exception e) {
         if (utx != null) {
            try {
               utx.rollback();
            } catch (Exception e1) {
            }
         }
      }
      return null;
   }

   public List<DisplayTweet> getRecentTweets() {
      LinkedList<DisplayTweet> recentTweets = new LinkedList<DisplayTweet>();
      List<String> following = auth.get().getUser().getWatching();

      long now = System.currentTimeMillis();
      
      //add initial entry (oldest possible one)
      recentTweets.add(new DisplayTweet());
      
      //first check only tweets newer than 1 hour, then increase maxAge
      for (int maxAge = 0; maxAge != ages.length; maxAge++) {
         if (recentTweets.size() >= RECENT_POSTS_LIMIT) {
            break;
         } 
         // get all people that I'm following
         for (String username : following) {
            User u = (User) getUserCache().get(username);
            LinkedList<TweetKey> tweetKeys = (LinkedList<TweetKey>) u.getTweets();
            Iterator<TweetKey> it = tweetKeys.descendingIterator();
            //go from newest to oldest tweet
            while(it.hasNext()) {
               TweetKey key = it.next();
               if (maxAge > 0 && key.getTimeOfPost() >= (now - ages[maxAge-1])) {
                  //if we checked this tweet in previous round, move on
                  continue;
               } else if (key.getTimeOfPost() < (now - ages[maxAge])) {
                  //if the tweet is older then what belongs to this run, move on
                  break;
               } 
               int position = 0;
               for (DisplayTweet recentTweet : recentTweets) {
                  if (key.getTimeOfPost() > recentTweet.getTimeOfPost()) {
                     Tweet t = (Tweet) getTweetCache().get(key);
                     DisplayTweet tw = new DisplayTweet(u.getName(), u.getUsername(), t.getMessage(),
                                       t.getTimeOfPost());
                     recentTweets.add(position, tw); // insert one position before -> get ordered collection
                     if (recentTweets.size() > RECENT_POSTS_LIMIT) {
                        recentTweets.removeLast();
                     }
                     break;
                  }
                  position++;
               }
            }
         }
      }
      long duration = System.currentTimeMillis() - now;
      System.out.println("Duration:" + duration);
      return recentTweets;
   }

   public List<DisplayTweet> getMyTweets() {
      LinkedList<DisplayTweet> myTweets = new LinkedList<DisplayTweet>();
      List<TweetKey> myTweetKeys = auth.get().getUser().getTweets();
      for (TweetKey key : myTweetKeys) {
         Tweet t = (Tweet) getTweetCache().get(key);
         DisplayTweet dispTweet = new DisplayTweet(auth.get().getUser().getName(), auth.get()
                  .getUser().getUsername(), t.getMessage(), t.getTimeOfPost());
         myTweets.addFirst(dispTweet);
      }
      return myTweets;
   }

   public List<DisplayTweet> getWatchedUserTweets() {
      LinkedList<DisplayTweet> userTweets = new LinkedList<DisplayTweet>();
      List<TweetKey> myTweetKeys = userBean.getWatchedUser().getTweets();
      for (TweetKey key : myTweetKeys) {
         Tweet t = (Tweet) getTweetCache().get(key);
         DisplayTweet dispTweet = new DisplayTweet(userBean.getWatchedUser().getName(), userBean
                  .getWatchedUser().getUsername(), t.getMessage(), t.getTimeOfPost());
         userTweets.addFirst(dispTweet);
      }
      return userTweets;
   }

   private void generateNewTweets() {
      Random r = new Random();
      try {
         utx.begin();
         for (int i = 1; i != 2; i++) {
            int id = (r.nextInt(100)) + 1; // do not return 0
            User u = (User) getUserCache().get("user" + id);
            Tweet t = new Tweet(u.getUsername(), "New tweet for " + u.getUsername());
            getTweetCache().put(t.getKey(), t);
            u.getTweets().add(t.getKey());
            getUserCache().replace(u.getUsername(), u);
         }
         utx.commit();
      } catch (Exception e) {
         if (utx != null) {
            try {
               utx.rollback();
            } catch (Exception e1) {
            }
         }
      }
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   private BasicCache<String, Object> getUserCache() {
      if (userCache != null) {
         return userCache;
      } else {
         return provider.getCacheContainer().getCache("userCache");
      }
   }

   private BasicCache<TweetKey, Object> getTweetCache() {
      if (tweetCache != null) {
         return tweetCache;
      } else {
         return provider.getCacheContainer().getCache("tweetCache");
      }
   }
}
