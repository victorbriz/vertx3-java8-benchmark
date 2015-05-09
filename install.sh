#!/bin/bash

fw_depends java8

RETCODE=$(fw_exists ${IROOT}/vert.x-3.0.0.installed)
[ ! "$RETCODE" == 0 ] || { return 0; }

fw_get http://dl.bintray.com/vertx/downloads/vert.x-3.0.0-milestone5.tar.gz?direct=true -O vert.x-3.0.0.tar.gz
fw_untar vert.x-3.0.0.tar.gz
wget http://central.maven.org/maven2/org/freemarker/freemarker/2.3.22/freemarker-2.3.22.jar -O ${IROOT}/vert.x-3.0.0/lib/freemarker-2.3.22.jar
touch ${IROOT}/vert.x-3.0.0.installed
