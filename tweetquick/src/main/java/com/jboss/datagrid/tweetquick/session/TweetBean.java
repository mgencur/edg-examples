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
import java.util.LinkedList;
import java.util.List;
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
      // generate some new tweets for random users to demonstrate users
      // behavior
      // generateNewTweets();

      List<String> following = auth.get().getUser().getWatching();

      // get all people that I'm following
      for (String username : following) {
         User u = (User) getUserCache().get(username);
         // get their tweets
         List<TweetKey> tweetKeys = u.getTweets();
         /*
          * for each key from all people that I follow, find out if the tweet is newer than what we
          * have, if yes, update our collection
          */
         for (TweetKey key : tweetKeys) {
            // System.out.println("For each tweetkey");
            Tweet t = (Tweet) getTweetCache().get(key);
            DisplayTweet tw = new DisplayTweet(u.getName(), u.getUsername(), t.getMessage(),
                     t.getTimeOfPost());
            if (recentTweets.size() == 0) {
               recentTweets.add(tw);
            } else {
               int counter = 0;
               for (DisplayTweet recentTweet : recentTweets) {
                  if (tw.getTimeOfPost() > recentTweet.getTimeOfPost()) {
                     // System.out.println("stored");
                     recentTweets.add(counter, tw); // insert one
                     // position before
                     // -> get ordered
                     // collection
                     if (recentTweets.size() >= RECENT_POSTS_LIMIT) {
                        recentTweets.removeLast();
                     }
                     break;
                  }
                  // System.out.println("test recent keys");
                  counter++;
               }
            }
         }
      } // end of for (String username : following)
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
