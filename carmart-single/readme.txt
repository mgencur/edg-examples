To run the example on JBoss AS 7 using remote EDG server:

1) add the following configuration to your $EDG_HOME/standalone/configuration/standalone.xml to configure
   remote datagrid (the application supposes that the datagrid is running on localhost)   

    <paths>
        <path name="temp" path="/tmp"/>
    </paths>
    
    ...right after <extensions> tag

    <local-cache name="carcache" start="EAGER" batching="false" indexing="NONE">
        <locking isolation="REPEATABLE_READ" striping="false" acquire-timeout="20000" concurrency-level="500"/>
        <eviction strategy="LIRS" max-entries="4"/>
        <file-store relative-to="temp" path="carstore" passivation="false"/>
    </local-cache>
    
    ...into infinispan sybsystem
   
2) start the EDG server
    
    $EDG_HOME/bin/standalone.sh

3) start JBoss AS 7 into which you want to deploy your application (this server is supposed to run on test1 address)

    $JBOSS_HOME/bin/standalone.sh

4) edit src/main/resources/META-INF/edg.properties file and specify address of the EDG server

    edg.address=localhost

5) in the example's directory:

* For remote access (client-server), run:

    mvn clean install     (this will deploy the resulting war into AS - via management interface running on test1:9990)
    
* For in-VM mode (which is not implemented and supported currently), run:

    mvn clean install -Plocal

* For remote access via REST interface, run:

    mvn clean install -Prest

6) go to http://test1:8080/carmart-single

NOTE: The application must be deployed into JBoss AS7, not EDG, since EDG does not support deploying applications. 
