Chunchun application
====================

Chunchun is a web application that demonstrates transactional access to several caches and shows how
in-memory data grid can effectively handle operations with lots of linked objects.

Name Chunchun [tʃu:n tʃu:n] comes from Maithili language which is a local dialect spoken in a part
of Nepal. It is a short high sound of a bird, similar to chirp or tweet.

Building and deploying to JBoss AS 7
------------------------------------

1) Start JBoss AS 7 where your application will run

    `$JBOSS_HOME/bin/standalone.sh`

2) Build the application

    `mvn clean package`

3) Deploy the application via jboss-as Maven plugin

    `mvn jboss-as:deploy`

4) Go to http://localhost:8080/chunchun

5) Undeploy the application

    `mvn jboss-as:undeploy`
