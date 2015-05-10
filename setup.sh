#!/bin/bash
# load java environment variables
source $IROOT/java8.installed

sed -i 's|host: \x27.*\x27|host: \x27'"${DBHOST}"'\x27|g' app.js

${IROOT}/vert.x-3.0.0-milestone5/bin/vertx run app.js &
