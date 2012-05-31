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
 * Handles tweet operations (sending tweets, listing recent tweets from all watched people,
 * listing own recent tweets, ...)
 * 
 * @author Martin Gencur
 * 
 */
@Named
@SessionScoped
public class TweetBean implements Serializable {

   private static final long serialVersionUID = -8914061755188086355L;

   private static final int INITIAL_POSTS_LIMIT = 30;
   private int loadedPosts = INITIAL_POSTS_LIMIT;
   private static final int INCREASE_LOADED_BY = 50; //increase loadedPosts by
   private int showedPosts = 10;
   private static final int INCREASE_SHOWED_BY = 10; //increase showedPosts by
   
   private static final long MINUTE = 60 * 1000;
   private static final long TEN_MINUTES = 10 * MINUTE;
   private static final long THIRTY_MINUTES = 30 * MINUTE;
   private static final long HOUR = 60 * MINUTE;
   private static final long TWELVE_HOURS = 12 * HOUR;
   private static final long DAY = 24 * HOUR;
   private static final long THREE_DAYS = DAY * 3;
   private static final long SEVEN_DAYS = DAY * 7;
   
   private static final long LOW_WATCH_LIMIT = 50;
   private static final long MEDIUM_WATCH_LIMIT = 300;
   private static final long HIGH_WATCH_LIMIT = 1000;

   //shorten time steps with increasing number of people that I'm watching
   private static long agesByNumOfWatched[][] = {
            { DAY, THREE_DAYS, SEVEN_DAYS }, 
            { HOUR, TWELVE_HOURS, DAY, THREE_DAYS, SEVEN_DAYS },
            { MINUTE, THIRTY_MINUTES, HOUR, DAY, SEVEN_DAYS },
            { MINUTE, TEN_MINUTES, THIRTY_MINUTES, TWELVE_HOURS, SEVEN_DAYS } };

   private String message;

   BasicCache<String, Object> userCache;

   BasicCache<TweetKey, Object> tweetCache;
   
   LinkedList<DisplayTweet> recentTweets = new LinkedList<DisplayTweet>();

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
      if (recentTweets.size() <= INITIAL_POSTS_LIMIT) {
          reloadTweets(INITIAL_POSTS_LIMIT);
      }
      if (showedPosts > loadedPosts) {
          loadedPosts += INCREASE_LOADED_BY;
          reloadTweets(loadedPosts);
      }
      return recentTweets.subList(0, showedPosts);
   }
   
   /*
    * Reload content of recentTweets list
    */
   private void reloadTweets(int limit) {
       long now = System.currentTimeMillis();
       List<String> following = auth.get().getUser().getWatching();
       long[] ages = chooseRecentTweetsStrategy(following.size());
       // add initial entry (oldest possible one)
       recentTweets.add(new DisplayTweet());
       // first check only tweets newer than 1 hour, then increase maxAge
       for (int maxAge = 0; maxAge != ages.length; maxAge++) {
          if (recentTweets.size() >= limit) {
             break;
          }
          // get all people that I'm following
          for (String username : following) {
             User u = (User) getUserCache().get(username);
             LinkedList<TweetKey> tweetKeys = (LinkedList<TweetKey>) u.getTweets();
             Iterator<TweetKey> it = tweetKeys.descendingIterator();
             // go from newest to oldest tweet
             while (it.hasNext()) {
                TweetKey key = it.next();
                //check only desired sector in the past
                if (maxAge > 0 && key.getTimeOfPost() >= (now - ages[maxAge - 1])) {
                   // if we checked this tweet in the previous sector, move on
                   continue;
                } else if (key.getTimeOfPost() < (now - ages[maxAge])) {
                   // if the tweet is older than what belongs to this sector, move on
                   break;
                }
                int position = 0;
                //possibly add the tweet to newest tweets
                for (DisplayTweet recentTweet : recentTweets) {
                   if (key.getTimeOfPost() > recentTweet.getTimeOfPost()) {
                      Tweet t = (Tweet) getTweetCache().get(key);
                      DisplayTweet tw = new DisplayTweet(u.getName(), u.getUsername(), t.getMessage(), t.getTimeOfPost());
                      recentTweets.add(position, tw); 
                      if (recentTweets.size() > limit) {
                         recentTweets.removeLast();
                      }
                      break;
                   }
                   position++;
                }
             }
          }
       }
   }

   public void moreTweets() {
       showedPosts += INCREASE_SHOWED_BY;
   }
   
   public int getDisplayedPosts() {
       return showedPosts;   
   }
   
   private long[] chooseRecentTweetsStrategy(int size) {
      if (size < LOW_WATCH_LIMIT) {
         return agesByNumOfWatched[0];
      } else if (size < MEDIUM_WATCH_LIMIT) {
         return agesByNumOfWatched[1];
      } else if (size < HIGH_WATCH_LIMIT) {
         return agesByNumOfWatched[2];
      } else {
         return agesByNumOfWatched[3];
      }
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
