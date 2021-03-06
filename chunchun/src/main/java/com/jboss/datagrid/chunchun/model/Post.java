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
package com.jboss.datagrid.chunchun.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Holds data for each post.
 * 
 * @author Martin Gencur
 * 
 */
public class Post implements Serializable {

   private static final long serialVersionUID = -3268797376991935010L;

   private PostKey key;
   
   private String message;

   public Post(String username, String message) {
      this.message = message;
      this.key = new PostKey(username, Calendar.getInstance().getTimeInMillis());
   }

   // for application initialization purposes
   public Post(String username, String message, long timestamp) {
      this.message = message;
      this.key = new PostKey(username, timestamp);
   }

   public PostKey getKey() {
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
