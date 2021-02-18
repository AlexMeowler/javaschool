# Logiweb
A web application that simulates the operation of the system of a certain company carrying out the transportation of goods.

# Codeship
[![Codeship Status for AlexMeowler/javaschool](https://app.codeship.com/projects/ae9cc548-5107-43bc-a4c0-60ee813c0211/status?branch=main)](https://app.codeship.com/projects/423888)

# VERSION 2.5.0

# HOW TO START

1. Download repository.
2. Go to logiweb/src/main/resources and create there 2 files: activemq.properties and db.properties  
Structure of activemq.properties file:  
brokerUrl=%AAA%  
username=%BBB%  
password=%CCC%  
Structure of db.properties file:  
hibernate.connection.username=%AAA%  
hibernate.connection.password=%BBB%  
hibernate.connection.url=%CCC%  
3. Deploy logiweb project to Tomcat Server.
4. Go to orders-table/servlet/src/main/java/org/retal/table/ws and open StatisticsService.java. Replace localhost:8080 with address of server where Logiweb will be deployed.
5. Go to orders-table/servlet/src/main/webapp/pages and open index.xhtml. Edit "port" attribute of <o:websocket> tag to the port your WildFly server is running on.
6. Deploy orders-table project to WildFly Server. Make sure the server has properly configured ActiveMQ resource adapter (and separate ActiveMQ server is started).
7. Fill DB with tables and data (structure of table has to be reverse engineered from org.retal.logiweb.domain package).
8. Done!

# SonarQube report (for version 2.4.0)
![SonarQube report](sonar.png?raw=true "SonarQube Report")

# CHANGES

A bit of code refactoring. made README.md look like readme of an average GitHub repository.

# TODO