FROM unidata/thredds-docker:4.6.10
RUN apt-get update && apt-get install -y python3 python3-pip
RUN pip3 install boto3 jinja2
COPY ingest.py /usr/local/src/ingest.py
COPY threddsConfig.xml /usr/local/tomcat/content/thredds/threddsConfig.xml
RUN echo 'refresh'
COPY ./src/templates/catalog.jinja /usr/local/src/catalog.jinja
COPY ./charles.cer /charles.cer
COPY ./charles.pem /charles.crt
RUN cp /charles.crt /usr/local/share/ca-certificates
RUN update-ca-certificates
RUN pip3 install certifi
RUN cat /charles.crt >> $(python3 -c "import certifi; print(certifi.where())")
ENV JAVA_OPTS -Dhttp.proxyHost=docker.for.mac.localhost -Dhttp.proxyPort=8888 -Dhttps.proxyHost=docker.for.mac.localhost -Dhttps.proxyPort=8888
COPY go.sh /go.sh
COPY target/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/tomcat/webapps/thredds/WEB-INF/lib/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar
ENTRYPOINT [""]
CMD ["/go.sh"]