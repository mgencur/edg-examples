/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import org.infinispan.Cache;
import org.jboss.edg.examples.carmartsingle.model.Car;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Martin Gencur
 * 
 */
@Model
public class CarManager
{
   public static final String CACHE_NAME = "carcache";
   public static final String CAR_NUMBERS_KEY = "carnumbers";
   
   @Inject
   private CacheContainerProvider provider;
   
   private Cache<String, Object> carCache;
   
   private String carId;
   private Car car = new Car();

   public CarManager() {
   }
   
   public String addNewCar() {
      carCache = provider.getCacheContainer().getCache(CACHE_NAME);
      carCache.put(car.getNumberPlate(), car);
      List<String> carNumbers = getNumberPlateList();
      if (carNumbers == null) carNumbers = new LinkedList<String>();
      carNumbers.add(car.getNumberPlate());
      carCache.replace(CAR_NUMBERS_KEY, carNumbers);
      return "home";
   }

   @SuppressWarnings("unchecked")
   private List<String> getNumberPlateList() {
      return (List<String>) carCache.get(CAR_NUMBERS_KEY);
   }
   
   public String showCarDetails(String numberPlate) {
      carCache = provider.getCacheContainer().getCache(CACHE_NAME);
      this.car = (Car) carCache.get(numberPlate);
      return "showdetails";
   }
   
   public List<String> getCarList() {
      //retrieve a cache
      carCache = provider.getCacheContainer().getCache(CACHE_NAME);
      //retrieve a list of number plates from the cache
      return getNumberPlateList();
   }
   
   public String removeCar(String numberPlate) {
       carCache = provider.getCacheContainer().getCache(CACHE_NAME);
       carCache.remove(numberPlate);
       List<String> carNumbers = getNumberPlateList();
       carNumbers.remove(numberPlate);
       carCache.replace(CAR_NUMBERS_KEY, carNumbers);
       return null;
   }
   
   public void setCarId(String carId) {
      this.carId = carId;
   }

   public String getCarId() {
      return carId;
   }

   public void setCar(Car car) {
      this.car = car;
   }

   public Car getCar() {
      return car;
   }
}
