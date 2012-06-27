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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Displays all information about posts and their owners in JSF pages.
 * 
 * @author Martin Gencur
 * 
 */
public class DisplayPost {

   private static final long serialVersionUID = 29993133022854381L;
   
   private static final long MS_PER_SECOND = 1000;
   
   private static final long MS_PER_MINUTE = 60 * MS_PER_SECOND;
   
   private static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
   
   private static final long MS_PER_DAY = 24 * MS_PER_HOUR;
   
   private static final SimpleDateFormat df = new SimpleDateFormat("d MMM");

   private String ownerName;
   
   private String ownerUsername;
   
   private String message;
   
   private long timeOfPost;

   public DisplayPost(String name, String username, String message, long timeOfPost) {
      this.ownerName = name;
      this.ownerUsername = username;
      this.message = message;
      this.timeOfPost = timeOfPost;
   }
   
   public DisplayPost() {
      this.ownerName = "";
      this.ownerUsername = "";
      this.message = "";
      this.timeOfPost = 0;
   }

   public String getOwnerName() {
      return ownerName;
   }

   public String getOwnerUsername() {
      return ownerUsername;
   }

   public String getMessage() {
      return message;
   }

   public String getFriendlyTimeOfPost() {
      Date now = Calendar.getInstance().getTime();

      long age = now.getTime() - getTimeOfPost();

      long days = (long) Math.floor(age / MS_PER_DAY);
      age -= (days * MS_PER_DAY);
      long hours = (long) Math.floor(age / MS_PER_HOUR);
      age -= (hours * MS_PER_HOUR);
      long minutes = (long) Math.floor(age / MS_PER_MINUTE);

      if (days < 7) {
         StringBuilder sb = new StringBuilder();

         if (days > 0) {
            sb.append(days);
            sb.append(days > 1 ? " days " : " day ");
         }

         if (hours > 0) {
            sb.append(hours);
            sb.append(hours > 1 ? " hrs " : " hr ");
         }

         if (minutes > 0) {
            sb.append(minutes);
            sb.append(minutes > 1 ? " minutes " : " minute ");
         }

         if (hours == 0 && minutes == 0) {
            sb.append("just now");
         } else {
            sb.append("ago");
         }

         return sb.toString();
      } else {
         return df.format(getTimeOfPost());
      }
   }

   public long getTimeOfPost() {
      return timeOfPost;
   }
}
