package com.jboss.datagrid.tweetquick.session;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

import org.infinispan.api.BasicCache;

import com.jboss.datagrid.tweetquick.model.User;

@Named
@SessionScoped
public class UserBean implements Serializable {

   private static final long serialVersionUID = -5419061180849357611L;

   @Inject
   private Instance<Authenticator> auth;

   private User watchedUser;

   @Inject
   private CacheContainerProvider provider;

   BasicCache<String, Object> userCache;

   @Inject
   private UserTransaction utx;

   public List<User> getWatching() {
      if (watchedUser == null) {
         watchedUser = auth.get().getUser();
      }
      List<User> returnWatching = new LinkedList<User>();
      List<String> watching = watchedUser.getWatching();
      for (String username : watching) {
         User u = (User) getUserCache().get(username);
         returnWatching.add(u);
      }
      return returnWatching;
   }

   public List<User> getWatchers() {
      if (watchedUser == null) {
         watchedUser = auth.get().getUser();
      }
      List<User> returnWatchers = new LinkedList<User>();
      List<String> watchers = watchedUser.getWatchers();
      for (String username : watchers) {
         User u = (User) getUserCache().get(username);
         returnWatchers.add(u);
      }
      return returnWatchers;
   }

   public String showUser(User user) {
      this.watchedUser = user;
      return "userdetails";
   }

   public String showUser(DisplayTweet tweet) {
      this.watchedUser = (User) getUserCache().get(tweet.getUsername());
      return "userdetails";
   }

   private BasicCache<String, Object> getUserCache() {
      if (userCache != null) {
         return userCache;
      } else {
         return provider.getCacheContainer().getCache("userCache");
      }
   }

   public User getWatchedUser() {
      return watchedUser;
   }

   public String goHome() {
      this.watchedUser = auth.get().getUser();
      return "home";
   }

   public boolean isWatchedByMe(User u) {
      List<String> watching = auth.get().getUser().getWatching();
      // System.out.println("User " + auth.get().getUser().getName() + " is watching: " +
      // auth.get().getUser().getWatching());
      // System.out.println("Koho: " + u.getUsername());
      // System.out.println("Test na watching: " + (watching.contains(u.getUsername()) ? true :
      // false));
      return watching.contains(u.getUsername()) ? true : false;
   }

   public String watchUser(User user) {
      User me = this.auth.get().getUser();
      List<String> watching = me.getWatching();
      watching.add(user.getUsername());
      try {
         utx.begin();
         getUserCache().replace(me.getUsername(), me);
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
}
