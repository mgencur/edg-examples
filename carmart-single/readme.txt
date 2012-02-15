To run the example on JBoss AS 7 using remote EDG server:
=========================================================

0) obtain EDG distribution with productized Infinispan libraries

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
   
2) start the EDG server (this server is supposed to run on test1 address)
    
    $EDG_HOME/bin/standalone.sh

3) start JBoss AS 7 into which you want to deploy your application

    $JBOSS_HOME/bin/standalone.sh

4) edit src/main/resources/META-INF/edg.properties file and specify address of the EDG server

    edg.address=test1

5) in the example's directory:

* For remote access using HotRod client, run:

    mvn clean install -Photrod (this will deploy the resulting war into AS - via management interface running on localhost:9999)

* For remote access via REST interface, run:

    mvn clean install -Prest
    
6) go to http://localhost:8080/carmart-single

NOTE: The application must be deployed into JBoss AS7, not EDG, since EDG does not support deploying applications. 

To run the example on JBoss AS 7 in library mode (Infinispan libraries bundled with the application):
============================================================================================

0) obtain EDG distribution with productized Infinispan libraries

1) install libraries from the bundle into your local maven repository

    mvn initialize -Pinit-repo -Dedg.dist=/path/to/edg/distribution
    
2) start JBoss AS 7 where your application will be running

    $JBOSS_HOME/bin/standalone.sh

3) build and deploy the application (deployed via a maven plugin connected to the management interface of AS)

    mvn clean install -Plocal

4) go to http://localhost:8080/carmart-single
