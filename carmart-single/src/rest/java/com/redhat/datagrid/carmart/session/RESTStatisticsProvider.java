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
package com.redhat.datagrid.carmart.session;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.redhat.datagrid.carmart.session.CacheContainerProvider;
import com.redhat.datagrid.carmart.session.StatisticsProvider;

/**
 * A Statistics provider that works over REST
 * 
 * @author Manik Surtani
 */
@Named("stats")
@RequestScoped
public class RESTStatisticsProvider implements StatisticsProvider {

    @Inject
    private CacheContainerProvider provider;

    @Override
    public void getStatsObject() {
        // Not supported over REST
    }

    @Override
    public String getRetrievals() {
        return "N/A";
    }

    @Override
    public String getStores() {
        return "N/A";
    }

    @Override
    public String getTotalEntries() {
        return "N/A";
    }

    @Override
    public String getHits() {
        return "N/A";
    }

    @Override
    public String getMisses() {
        return "N/A";
    }

    @Override
    public String getRemoveHits() {
        return "N/A";
    }
}
