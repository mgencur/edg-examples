package org.jboss.edg.examples.carmartsingle.session;

import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;

/**
 * 
 * @author Martin Gencur
 *
 */
@ApplicationScoped
public class LocalCacheContainerProvider implements CacheContainerProvider
{
   private Logger log = Logger.getLogger(this.getClass().getName());
   
   private boolean created = false;
   
   private CacheContainer manager;
   
   public CacheContainer getCacheContainer() {
      if (!created) {
         manager = new DefaultCacheManager();
         created = true;
         log.info("=== Using DefaultCacheManager (in-VM mode) ===");
      }
      return manager;
   }
     
   @PreDestroy
   public void cleanUp() {
      manager.stop();
   }
}
