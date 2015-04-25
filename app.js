var persistorConf = {
  address: 'hello.persistor',
  db_name: 'hello_world',
  host: '127.0.0.1',
  pool_size: 20
}

var webserverConf = {
  multi_threaded: "true",
  instances: java.lang.Runtime.getRuntime().availableProcessors() * 2
}

vertx.deployVerticle('service:io.vertx.mongo-service', persistorConf, function (res, res_err) {
  if (!res_err) {
    vertx.deployVerticle('WebServer.java', webserverConf);
  } else {
    res_err.printStackTrace();
  }
});

