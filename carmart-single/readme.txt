To run the example on JBoss AS 7 with EDG:

1) add the following configuration to you $JBOSS_HOME/standalone/configuration/infinispan-configuration.xml:

<namedCache name="carcache">
      
      <eviction wakeUpInterval="500"
                maxEntries="4"
                strategy="LIRS"/>

      <loaders passivation="false"
               shared="false"
               preload="true">

         <loader class="org.infinispan.loaders.file.FileCacheStore"
                 fetchPersistentState="false"
                 ignoreModifications="false"
                 purgeOnStartup="false">

            <properties>
               <property name="location"
                         value="${java.io.tmpdir}"
                         />
            </properties>

            <async enabled="false" />
         </loader>
      </loaders>
   </namedCache>
   
2) start the application server
    
    $JBOSS_HOME/bin/standalone.sh

3) in the example's directory:

* For remote access (client-server), run:

    mvn clean install     (this will deploy the resulting war into AS)
    
* For in-VM mode (which is not implemented and supported currently), run:

    mvn clean install -Plocal

4) go to http://localhost:8080/carmart-single
