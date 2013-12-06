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
package com.jboss.datagrid.carmart.jsf;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.infinispan.commons.api.BasicCache;
import com.jboss.datagrid.carmart.model.Car;
import com.jboss.datagrid.carmart.model.Car.CarType;
import com.jboss.datagrid.carmart.model.Car.Country;
import com.jboss.datagrid.carmart.session.CacheContainerProvider;
import com.jboss.datagrid.carmart.session.CarManager;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.UserTransaction;

/**
 * Populates a cache with initial data. We need to obtain BeanManager from JNDI and create an instance of CacheContainerProvider
 * manually since injection into Listeners is not supported by CDI specification.
 * 
 * @author Martin Gencur
 * 
 */
public class PopulateCache implements SystemEventListener {

    private Logger log = Logger.getLogger(this.getClass().getName());

    private CacheContainerProvider provider;
    
    private UserTransaction utx;

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        provider = getContextualInstance(getBeanManagerFromJNDI(), CacheContainerProvider.class);
        startup();
    }

    public void startup() {
        BasicCache<String, Object> cars = provider.getCacheContainer().getCache(CarManager.CAR_CACHE_NAME);
        BasicCache<String, Object> carList = provider.getCacheContainer().getCache(CarManager.CAR_LIST_CACHE_NAME);
        List<String> carNumbers = new ArrayList<String>();
        
        utx = getUserTransactionFromJNDI();
        
        try {
            utx.begin();
            Car c = new Car("Ford Focus", 1.6, CarType.COMBI, "white", "FML 23-25", Country.CZECH_REPUBLIC);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("BMW X3", 2.0, CarType.SEDAN, "gray", "1P3 2632", Country.CZECH_REPUBLIC);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("Ford Mondeo", 2.2, CarType.COMBI, "blue", "1B2 1111", Country.USA);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("Mazda MX-5", 1.8, CarType.CABRIO, "red", "6T4 2526", Country.USA);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("VW Golf", 1.6, CarType.HATCHBACK, "yellow", "2B2 4946", Country.GERMANY);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("VW Passat", 2.0, CarType.COMBI, "black", "7T7 7777", Country.GERMANY);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("Renault Clio", 1.8, CarType.HATCHBACK, "blue", "8E6 8456", Country.CZECH_REPUBLIC);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("Renault Megane", 2.0, CarType.SEDAN, "red", "6A6 6666", Country.CZECH_REPUBLIC);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            // insert a list of cars' number plates
            carList.put(CarManager.CAR_NUMBERS_KEY, carNumbers);
            utx.commit();
            log.info("Successfully imported data!");
        } catch (Exception e) {
            log.warning("An exception occured while populating the database! Rolling back the transaction.");
            if (utx != null) {
                try {
                    utx.rollback();
                } catch (Exception e1) {
                }
            }
        }
    }

    private BeanManager getBeanManagerFromJNDI() {
        InitialContext context;
        Object result;
        try {
            context = new InitialContext();
            result = context.lookup("java:comp/BeanManager"); // lookup in JBossAS
        } catch (NamingException e) {
            throw new RuntimeException("BeanManager could not be found in JNDI", e);
        }
        return (BeanManager) result;
    }
    
    private UserTransaction getUserTransactionFromJNDI() {
        InitialContext context;
        Object result;
        try {
            context = new InitialContext();
            result = context.lookup("java:comp/UserTransaction"); // lookup in JBossAS
        } catch (NamingException ex) {
            throw new RuntimeException("UserTransaction could not be found in JNDI", ex);
        }
        return (UserTransaction) result;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextualInstance(final BeanManager manager, final Class<T> type) {
        T result = null;
        Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type));
        if (bean != null) {
            CreationalContext<T> context = manager.createCreationalContext(bean);
            if (context != null) {
                result = (T) manager.getReference(bean, type, context);
            }
        }
        return result;
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof Application;
    }
}
