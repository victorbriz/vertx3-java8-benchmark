import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class WebServer extends AbstractVerticle {

  private final String DATE_FORMAT_STRING = "EEE, dd MMM yyyyy HH:mm:ss z";
  private final String PATH_PLAINTEXT = "/plaintext";
  private final String PATH_JSON = "/json";
  private final String PATH_DB = "/db";
  private final String PATH_QUERIES = "/queries";
  private final String RESPONSE_TYPE_PLAIN = "text/plain";
  private final String RESPONSE_TYPE_JSON = "application/json";
  private final String HEADER_CONTENT_TYPE = "Content-Type";
  private final String HEADER_CONTENT_LENGTH = "Content-Length";
  private final String HEADER_SERVER = "Server";
  private final String HEADER_SERVER_VERTX = "vert.x";
  private final String HEADER_DATE = "Date";
  private final String MONGO_ADDRESS = "hello.persistor";
  private final String UNDERSCORE_ID = "_id";
  private final String TEXT_ID = "id";
  private final String TEXT_RESULT = "result";
  private final String TEXT_QUERIES = "queries";
  private final String TEXT_MESSAGE = "message";
  private final String HELLO_WORLD = "Hello, world!";
  private final String TEXT_ACTION = "action";
  private final String TEXT_FINDONE = "findone";
  private final String TEXT_COLLECTION = "collection";
  private final String TEXT_WORLD = "World";
  private final String TEXT_MATCHER = "matcher";

  private final Buffer helloWorldBuffer = Buffer.buffer(HELLO_WORLD);
  private final String helloWorldContentLength = String.valueOf(helloWorldBuffer.length());
  private final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
  private String dateString;

  @Override
  public void start() {
    vertx.createHttpServer().requestHandler(req -> {
    switch (req.path()) {
      case PATH_PLAINTEXT:
        handlePlainText(req);
        break;
      case PATH_JSON:
        handleJson(req);
        break;
      case PATH_DB:
        handleDbMongo(req);
        break;
      case PATH_QUERIES:
        handleQueriesMongo(req);
        break;
      default:
        req.response().setStatusCode(404);
        req.response().end();
    }
    }).listen(8080);
    vertx.setPeriodic(1000, new Handler<Long>() {
      @Override
      public void handle(final Long timerID) {
        formatDate();
      }
    });
    formatDate();
  }

  private void formatDate() {
    dateString = DATE_FORMAT.format(new Date());
  }

  private void handlePlainText(final HttpServerRequest req) {
    HttpServerResponse resp = req.response();
    setHeaders(resp, RESPONSE_TYPE_PLAIN, helloWorldContentLength);
    resp.end(helloWorldBuffer);
  }

  private void handleJson(final HttpServerRequest req) {
    Buffer buff = Buffer.buffer(Json.encode(Collections.singletonMap(TEXT_MESSAGE, HELLO_WORLD)));
    HttpServerResponse resp = req.response();
    setHeaders(resp, RESPONSE_TYPE_JSON, String.valueOf(buff.length()));
    resp.end(buff);
  }

  private void handleDbMongo(final HttpServerRequest req) {
    vertx.eventBus().send(
        MONGO_ADDRESS,
        new JsonObject()
            .put(TEXT_ACTION, TEXT_FINDONE)
            .put(TEXT_COLLECTION, TEXT_WORLD)
            .put(TEXT_MATCHER, new JsonObject().put(UNDERSCORE_ID, (ThreadLocalRandom.current().nextInt(10000) + 1))),
        res -> {
          JsonObject world = getResultFromReply(res.result());
          String result = world.encode();
          sendResponse(req, result);
    });
  }
  
  private JsonObject getResultFromReply(final Message reply) {
    JsonObject body = (JsonObject)reply.body();
    JsonObject world = body.getJsonObject(TEXT_RESULT);
    Object id = world.remove(UNDERSCORE_ID);
    if (id instanceof Double) {
      world.put(TEXT_ID, Integer.valueOf(((Double)id).intValue()));
    } else {
      world.put(TEXT_ID, id);
    }
    return world;
  }

  private void handleQueriesMongo(final HttpServerRequest req) {
    int queriesParam = 1;
    try {
      queriesParam = Integer.parseInt(req.params().get(TEXT_QUERIES));
    } catch (NumberFormatException e) {
      queriesParam = 1;
    }
    if (queriesParam < 1) {
      queriesParam = 1;
    } else if (queriesParam > 500) {
      queriesParam = 500;
    }
    final JsonArray worlds = new JsonArray();
    for (int i = 0; i < queriesParam; i++) {
      vertx.eventBus().send(
          MONGO_ADDRESS,
          new JsonObject()
              .put(TEXT_ACTION, TEXT_FINDONE)
              .put(TEXT_COLLECTION, TEXT_WORLD)
              .put(TEXT_MATCHER, new JsonObject().put(UNDERSCORE_ID, (ThreadLocalRandom.current().nextInt(10000) + 1))),
          res -> {
            JsonObject world = getResultFromReply(res.result());
            worlds.add(world);
      });
    }
    String result = worlds.encode();
    sendResponse(req, result);
  }

  private void sendResponse(final HttpServerRequest req, final String result) {
    Buffer buff = Buffer.buffer(result);
    HttpServerResponse resp = req.response();
    setHeaders(resp, RESPONSE_TYPE_JSON, String.valueOf(buff.length()));
    resp.end(buff);
  }

  private void setHeaders(final HttpServerResponse resp, final String contentType, final String contentLength) {
    resp.putHeader(HEADER_CONTENT_TYPE, contentType);
    resp.putHeader(HEADER_CONTENT_LENGTH, contentLength);
    resp.putHeader(HEADER_SERVER, HEADER_SERVER_VERTX );
    resp.putHeader(HEADER_DATE, dateString);
  }
}

