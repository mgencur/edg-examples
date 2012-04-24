package com.jboss.datagrid.tweetquick.session;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.infinispan.api.BasicCache;

import com.jboss.datagrid.tweetquick.jsf.InitializeCache;
import com.jboss.datagrid.tweetquick.model.User;

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
