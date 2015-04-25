#!/bin/bash

fw_depends java8

RETCODE=$(fw_exists ${IROOT}/vert.x-3.0.0-milestone4.installed)
[ ! "$RETCODE" == 0 ] || { return 0; }

fw_get http://dl.bintray.com/vertx/downloads/vert.x-3.0.0-milestone4.tar.gz?direct=true -O vert.x-3.0.0-milestone4.tar.gz
fw_untar vert.x-3.0.0-milestone4.tar.gz

touch ${IROOT}/vert.x-3.0.0-milestone4.installed
