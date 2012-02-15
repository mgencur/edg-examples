package com.redhat.datagrid.carmart.session;

import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.api.BasicCacheContainer;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.util.concurrent.IsolationLevel;
import com.redhat.datagrid.carmart.session.CacheContainerProvider;

/**
 * {@link CacheContainerProvider}'s implementation creating a DefaultCacheManager 
 * which is configured programmatically. Infinispan's libraries need to be bundled
 * with the application - this is called "library" mode.
 *  
 * 
 * @author Martin Gencur
 *
 */
@ApplicationScoped
public class LocalCacheContainerProvider extends CacheContainerProvider
{
   private Logger log = Logger.getLogger(this.getClass().getName());
   
   private BasicCacheContainer manager;
   
   public BasicCacheContainer getCacheContainer() {
      if (manager == null) {
          GlobalConfiguration glob = new GlobalConfigurationBuilder()
              .nonClusteredDefault()
              .globalJmxStatistics().enable()
              .build();
          Configuration loc = new ConfigurationBuilder()
              .jmxStatistics().enable()
              .clustering()
              .cacheMode(CacheMode.LOCAL)
              .locking().isolationLevel(IsolationLevel.REPEATABLE_READ)
              .eviction().maxEntries(4).strategy(EvictionStrategy.LIRS)
              .loaders().passivation(false).addFileCacheStore().purgeOnStartup(true)
              .build();
          manager = new DefaultCacheManager(glob, loc, true);
          log.info("=== Using DefaultCacheManager (library mode) ===");
      }
      return manager;
   }
     
   @PreDestroy
   public void cleanUp() {
      manager.stop();
      manager = null;
   }
}
