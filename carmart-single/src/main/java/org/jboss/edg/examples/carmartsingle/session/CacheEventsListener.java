/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.edg.examples.carmartsingle.session;

import javax.inject.Inject;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryLoaded;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryVisited;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvictedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryLoadedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryVisitedEvent;

@Listener
public class CacheEventsListener {

//   @Inject
//   private CacheEventsRecorder r;
//
//   @CacheEntryCreated
//   public void recordEntryCreation(CacheEntryCreatedEvent event) {
//      r.recordCreation();
//   }
//
//   @CacheEntryModified
//   public void recordEntryModification(CacheEntryModifiedEvent event) {
//      r.recordModification();
//   }
//
//   @CacheEntryRemoved
//   public void recordEntryRemoval(CacheEntryRemovedEvent event) {
//      r.recordRemoval();
//   }
//
//   @CacheEntryVisited
//   public void recordEntryVisitation(CacheEntryVisitedEvent event) {
//      r.recordVisitation();
//   }
//
//   @CacheEntryLoaded
//   public void recordEntryLoading(CacheEntryLoadedEvent event) {
//      r.recordLoading();
//   }
//
//   @CacheEntryEvicted
//   public void recordEntryEviction(CacheEntryEvictedEvent event) {
//      r.recordEviction();
//   }
}
