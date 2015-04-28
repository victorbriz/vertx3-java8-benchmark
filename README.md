# Vertx 3.x Benchmarking Test

This is the vertx 3.x portion of a [benchmarking test suite](../) comparing a variety of web development platforms.

### Plaintext Test

* [Plaintext test source](WebServer.java)

### JSON Serialization Test

* [JSON test source](WebServer.java)

### Database Single query Test

* [Database Single query test source](WebServer.java)

### Database Multiple queries Test

* [Database Multiple queries test source](WebServer.java)

### Database Data updates Test

* [Database Data updates test source](WebServer.java)

### Fortunes Test

* [Fortunes test source](WebServer.java)

## Versions

* [Oracle Java 8](http://oracle.com/)
* [vertx 3.0.0](http://vertx.io/)

## Test URLs

### Plaintext Test

    http://localhost:8080/plaintext

### JSON Encoding Test

    http://localhost:8080/json

### Database Mapping Test

    http://localhost:8080/db?queries=5

### Database Single query Test

    http://localhost:8080/db

### Database Multiple queries Test

    http://localhost:8080/queries?queries=5

### Database Data updates Test

    http://localhost:8080/updates?queries=3

### Fortunes Test

    http://localhost:8080/fortunes
