How to run the example
======================

Building and deploying to JBoss AS 7
------------------------------------

0) Obtain JDG distribution with productized Infinispan libraries (library distribution)

1) Install libraries from the bundle into your local maven repository

    `mvn initialize -Pinit-repo -Ddatagrid.dist=/home/anyuser/jboss-datagrid-library-6.0.0.ER4-redhat-1`
    
2) Start JBoss AS 7 where your application will run

    `$JBOSS_HOME/bin/standalone.sh`

3) Build the application

    `mvn clean package`

4) Deploy the application via jboss-as Maven plugin

    `mvn jboss-as:deploy`

5) Go to http://localhost:8080/tweetquick

6) Undeploy the application

    `mvn jboss-as:undeploy`
