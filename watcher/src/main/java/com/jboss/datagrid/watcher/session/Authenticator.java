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
package com.jboss.datagrid.watcher.session;

import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.infinispan.api.BasicCache;
import com.jboss.datagrid.watcher.jsf.InitializeCache;
import com.jboss.datagrid.watcher.model.User;

/**
 * Authenticates a user by username/password.
 * 
 * @author Martin Gencur
 * 
 */
@Named("auth")
@SessionScoped
public class Authenticator implements Serializable {

   private static final long serialVersionUID = 8144862964879581999L;

   private User user;

   private String username = "user1";
   
   private String password = "pass1";

   @Inject
   private CacheContainerProvider provider;

   public boolean isLoggedIn() {
      return user != null;
   }

   public void login() {
      BasicCache<String, Object> userCache = provider.getCacheContainer().getCache("userCache");
      User currentUser = (User) userCache.get(username);
      if (currentUser == null) {
         FacesContext.getCurrentInstance().addMessage("msg1",
                  new FacesMessage("Username does not exist!"));
         return;
      }
      String storedEncryptedPassword = currentUser.getPassword();
      String currentEncryptedPassword = InitializeCache.hashPassword(password);
      if (storedEncryptedPassword.equals(currentEncryptedPassword)) {
         user = currentUser;
      } else {
         FacesContext.getCurrentInstance().addMessage("msg2", new FacesMessage("Wrong password!"));
      }
   }

   public void logout() {
      user = null;
      /* call authenticate() to redirect immediately and prevent JSF from rendering response and then
         calling authenticate as a pageAction again (which leads to IllegalStateException) */
      authenticate(); 
   }

   public void logoutFromServlet() {
      user = null;
   }

   public void authenticate() {
      if (!isLoggedIn()) {
         try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.jsf");
         } catch (IOException e) {
            throw new RuntimeException("Redirection failed.");
         }
      }
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public User getUser() {
      return user;
   }
}
