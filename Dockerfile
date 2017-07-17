FROM unidata/thredds-docker:4.6.10
COPY catalog.xml /usr/local/tomcat/content/thredds/catalog.xml
COPY threddsConfig.xml /usr/local/tomcat/content/thredds/threddsConfig.xml
COPY --from=0 target/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/tomcat/webapps/thredds/WEB-INF/lib/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar
