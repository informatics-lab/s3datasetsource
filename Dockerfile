# FROM maven:3-jdk-8
# COPY src src
# COPY pom.xml pom.xml
# RUN mvn clean install

FROM unidata/thredds-docker:4.6.10
COPY catalog.xml /usr/local/tomcat/content/thredds/catalog.xml
COPY threddsConfig.xml /usr/local/tomcat/content/thredds/threddsConfig.xml
COPY --from=0 target/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/tomcat/webapps/thredds/WEB-INF/lib/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar