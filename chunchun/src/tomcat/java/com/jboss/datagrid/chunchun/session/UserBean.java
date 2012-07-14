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
package com.jboss.datagrid.chunchun.session;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.TransactionManager;

import org.infinispan.CacheImpl;
import org.infinispan.api.BasicCache;
import com.jboss.datagrid.chunchun.model.User;

/**
 * Handles operations with users (retrieving users from a cache, getting 
 * watched/watching users, ...)
 * 
 * @author Martin Gencur
 * 
 */
@Named
@RequestScoped
public class UserBean implements Serializable {

   private static final long serialVersionUID = -5419061180849357611L;

   @Inject
   private Instance<Authenticator> auth;

   private User watchedUser;

   @Inject
   private CacheContainerProvider provider;

   BasicCache<String, Object> userCache;
   
   @PostConstruct
   public void initialize() {
      watchedUser = auth.get().getUser(); 
   }

   private TransactionManager tm;
   
   public void showUserImage(OutputStream out, Object data) {
      try {
         User u = (User) getUserCache().get((String) data);
         ImageIO.write(u.getAvatar(), "jpg", out);
      } catch (Exception e) {
         throw new RuntimeException("Unable to load data for image", e);
      }
   }
   
   public List<User> getWatching() {
      List<User> returnWatching = new LinkedList<User>();
      List<String> watching = watchedUser.getWatching();
      for (String username : watching) {
         User u = (User) getUserCache().get(username);
         returnWatching.add(u);
      }
      return returnWatching;
   }

   public List<User> getWatchers() {
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

   public String showUser(DisplayPost post) {
      this.watchedUser = (User) getUserCache().get(post.getOwnerUsername());
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
      return watching.contains(u.getUsername()) ? true : false;
   }

   public String watchUser(User user) {
      User me = this.auth.get().getUser();
      tm = getTransactionManager(getUserCache());
      List<String> watching = me.getWatching();
      watching.add(user.getUsername());
      try {
         tm.begin();
         getUserCache().replace(me.getUsername(), me);
         tm.commit();
      } catch (Exception e) {
         if (tm != null) {
            try {
               tm.rollback();
            } catch (Exception e1) {
            }
         }
      }
      return null;
   }
   
   public String stopWatchingUser(User user) {
      User me = this.auth.get().getUser();
      tm = getTransactionManager(getUserCache());
      List<String> watching = me.getWatching();
      watching.remove(user.getUsername());
      try {
         tm.begin();
         getUserCache().replace(me.getUsername(), me);
         tm.commit();
      } catch (Exception e) {
         if (tm != null) {
            try {
               tm.rollback();
            } catch (Exception e1) {
            }
         }
      }
      return null;
   }

   private TransactionManager getTransactionManager(BasicCache<?, ?> cache) {
      TransactionManager tm = ((CacheImpl) cache).getAdvancedCache().getTransactionManager();
      return tm;
   }
}
