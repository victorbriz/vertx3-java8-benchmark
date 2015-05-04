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
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.core.AsyncResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import freemarker.template.Template;
import freemarker.template.Configuration;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class WebServer extends AbstractVerticle {

  private static final String PATH_PLAINTEXT = "/plaintext";
  private static final String PATH_JSON = "/json";
  private static final String PATH_DB = "/db";
  private static final String PATH_QUERIES = "/queries";
  private static final String PATH_UPDATES = "/updates";
  private static final String PATH_FORTUNES = "/fortunes";
  private static final String RESPONSE_TYPE_PLAIN = "text/plain";
  private static final String RESPONSE_TYPE_HTML = "text/html";
  private static final String RESPONSE_TYPE_JSON = "application/json";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_CONTENT_LENGTH = "Content-Length";
  private static final String HEADER_SERVER = "Server";
  private static final String HEADER_SERVER_VERTX = "vert.x";
  private static final String HEADER_DATE = "Date";
  private static final String MONGO_ADDRESS = "hello.persistor";
  private static final String FREEMARKER_ADDRESS = "vertx.freemarker";
  private static final String UNDERSCORE_ID = "_id";
  private static final String TEXT_ID = "id";
  private static final String RANDOM_NUMBER = "randomNumber";
  private static final String TEXT_RESULT = "result";
  private static final String TEXT_RESULTS = "results";
  private static final String TEXT_QUERIES = "queries";
  private static final String TEXT_MESSAGE = "message";
  private static final String TEXT_MESSAGES = "messages";
  private static final String ADD_FORTUNE_MESSAGE = "Additional fortune added at request time.";
  private static final String HELLO_WORLD = "Hello, world!";
  private static final String TEXT_ACTION = "action";
  private static final String TEXT_CRITERIA = "criteria";
  private static final String TEXT_UPDATE = "update";
  private static final String TEXT_OBJ_NEW = "objNew";
  private static final String TEXT_FINDONE = "findone";
  private static final String TEXT_FIND = "find";
  private static final String TEXT_COLLECTION = "collection";
  private static final String TEXT_WORLD = "World";
  private static final String TEXT_FORTUNE = "Fortune";
  private static final String TEXT_MATCHER = "matcher";
  private static final String TEMPLATE_FORTUNE = "<!DOCTYPE html><html><head><title>Fortunes</title></head><body><table><tr><th>id</th><th>message</th></tr><#list messages as message><tr><td>${message.id?html}</td><td>${message.message?html}</td></tr></#list></table></body></html>";
  private static final String TEXT_$SET = "$set";
  private static final String DATE_FORMAT_STRING = "EEE, dd MMM yyyyy HH:mm:ss z";
  private static final String MONGODB_CONFIG
            = "{"
            + "    \"address\": \"hello_persistor\","
            + "    \"host\": \"127.0.0.1\","
            + "    \"port\": 27017,"
            + "    \"db_name\": \"hello_world\","
            + "    \"pool_size\": 20,"
            + "    \"useObjectId\" : true"
            + "}";  

  private final Buffer helloWorldBuffer = Buffer.buffer(HELLO_WORLD);
  private final String helloWorldContentLength = String.valueOf(helloWorldBuffer.length());
  private final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
  private final Random random = ThreadLocalRandom.current();
  private String dateString;
  private MongoService mongoService;
  private Template ftlTemplate;

  @Override
  public void start() {
    try { ftlTemplate = new Template(TEXT_FORTUNE, new StringReader(TEMPLATE_FORTUNE), new Configuration(Configuration.VERSION_2_3_22)); } catch (Exception ex) { ex.printStackTrace(); }
    mongoService = MongoService.create(vertx, new JsonObject(MONGODB_CONFIG));
    mongoService.start();
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
        handleDBMongo(req,false);
        break;
      case PATH_UPDATES:
        handleDBMongo(req,true);
        break;
      case PATH_FORTUNES:
        handleFortunes(req);
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
    JsonObject query = new JsonObject().put(TEXT_ID, (random.nextInt(10000) + 1));
    mongoService.find(TEXT_WORLD, query, res -> {
      if (res.succeeded() && res.result().size() > 0) {
        sendResponse(req, getResultFromReply(res.result().get(0)).encode());
      } else {
        res.cause().printStackTrace();
      }
    });
  }
  
  private JsonObject getResultFromReply(final JsonObject json) {
    Object id = json.remove(UNDERSCORE_ID);
    json.put(TEXT_ID, Integer.valueOf(((Double)id).intValue()));
    return json;
  }

  private void handleFortunes(HttpServerRequest req) {
    final HttpServerResponse resp = req.response();
    JsonObject query = new JsonObject();
    mongoService.find(TEXT_FORTUNE, query, res -> {
      if (res.succeeded() && res.result().size() > 0) {
        List<Fortune> fortunes = new ArrayList<>();
          for (JsonObject fortune: res.result()) {
            fortunes.add(new Fortune(
              fortune.getInteger(TEXT_ID),
              fortune.getString(TEXT_MESSAGE)));
        }            
        fortunes.add(new Fortune(0, ADD_FORTUNE_MESSAGE));
        Collections.sort(fortunes);

        Map model = new HashMap();
        model.put(TEXT_MESSAGES, fortunes);
        Writer writer = new StringWriter();
        try { ftlTemplate.process(model, writer); } catch (Exception ex) { ex.printStackTrace(); }

        Buffer buff = Buffer.buffer(writer.toString());
        setHeaders(resp, RESPONSE_TYPE_HTML, String.valueOf(buff.length()));
        resp.end(buff);
      }  
    });
  }

  private void handleDBMongo(final HttpServerRequest req, final boolean randomUpdates) {
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
      JsonObject query = new JsonObject().put(TEXT_ID, (random.nextInt(10000) + 1));
      mongoService.find(TEXT_WORLD, query, res -> {
        if (res.succeeded() && res.result().size() > 0) {
          JsonObject world = getResultFromReply(res.result().get(0));
          if (randomUpdates) {
            world.put(RANDOM_NUMBER, (random.nextInt(10000) + 1));
            updateRandom(world);
          }
          worlds.add(world);
        } else {
          res.cause().printStackTrace();
        }
      });
    } 
    sendResponse(req, worlds.encode());
  }

  private void updateRandom(JsonObject json) {
    JsonObject query = new JsonObject().put(UNDERSCORE_ID, json.getString(TEXT_ID));
    JsonObject update = new JsonObject().put(TEXT_$SET, json);
    mongoService.update(TEXT_WORLD, query, update, res -> {
      if (!res.succeeded()) {
        res.cause().printStackTrace();
      }
    });
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

  public final class Fortune implements Comparable<Fortune> {
    public int id;
    public String message;

    public int getId() {
      return id;
    }
    public String getMessage() {
      return message;
    }
    public Fortune(int id, String message) {
      this.id = id;
      this.message = message;
    }
    @Override
    public int compareTo(Fortune other) {
      return message.compareTo(other.message);
    }
  }  
}

