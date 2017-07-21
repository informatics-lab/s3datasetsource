#! /bin/bash
/usr/bin/env python3 /usr/local/src/ingest.py /usr/local/tomcat/content/thredds/catalog.xml
/entrypoint.sh catalina.sh run