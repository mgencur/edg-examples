How to run the example
======================

This application demonstrates transactional access to several caches and shows how
in-memory data grid can effectively handle operations with lots of linked objects. 

Building and deploying to JBoss AS 7
------------------------------------

1) Start JBoss AS 7 where your application will run

    `$JBOSS_HOME/bin/standalone.sh`

2) Build the application

    `mvn clean package`

3) Deploy the application via jboss-as Maven plugin

    `mvn jboss-as:deploy`

4) Go to http://localhost:8080/watcher

5) Undeploy the application

    `mvn jboss-as:undeploy`
