package edu.brown.cs.student.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  private static final int DEFAULT_PORT = 4567;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() throws SQLException, ClassNotFoundException {

    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);
    parser.accepts("prefix");
    parser.accepts("whitespace");
    parser.accepts("stats");
    OptionSpec<Integer> ledSpec =
        parser.accepts("led").withRequiredArg().ofType(Integer.class);
    OptionSpec<String> dataSpec =
        parser.accepts("data").withRequiredArg().ofType(String.class);

    OptionSpec<String> databaseSpec =
        parser.accepts("database").withRequiredArg().ofType(String.class);
    OptionSpec<String> deleteSpec = parser.accepts("delete").withRequiredArg().ofType(String.class);


    OptionSet options = parser.parse(args);

    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));

    } else if (options.has("data") || options.has("database")) {

      boolean prefix = false;
      boolean whitespace = false;
      int led = 0;

      if (options.has("prefix")) {
        prefix = true;
      }
      if (options.has("whitespace")) {
        whitespace = true;
      }
      if (options.has("led")) {
        led = (int) options.valueOf(ledSpec);
      }

      Autocorrector ac = null;
      Database db;


      if (options.has("database")) {

        try {
          db = new Database(options.valueOf(databaseSpec));

          if (options.has("data")) {

            String files = options.valueOf(dataSpec);
            List<String> fileNames = new ArrayList<String>(Arrays.asList(files.split(",")));

            for (String file : fileNames) {
              db.readCorpus(file);
            }
          } else {
            db.readAll();
          }
          ac = new Autocorrector(db, prefix, whitespace, led);

          if (options.has("stats")) {
            Map<String, Integer> freq = db.getFrequencyMap();
            System.out.println("\u001b[35mCorpus Statistics:\u001b[0m");
            for (String key : freq.keySet()) {
              System.out.println(key + " : " + freq.get(key));
            }

            Map<String, Integer> occur = db.getInstanceMap();
            System.out.println("\u001b[35mWord Statistics:\u001b[0m");
            for (String key : occur.keySet()) {
              System.out.println(key + " : " + occur.get(key));
            }

          }
        } catch (SQLException e) {
          System.err.println("SQLite error: " + e.getMessage());
          System.exit(1);
        }
      } else {
        String files = options.valueOf(dataSpec);
        ac = new Autocorrector(files, prefix, whitespace, led);
      }


      // Create autocorrector using files and flags passed in.

      // For each line of input from user, output autocorrect suggestions.
      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(System.in))) {
        String input;
        while ((input = br.readLine()) != null) {
          Set<String> suggestions = ac.suggest(input);
          for (String s : suggestions) {
            System.out.println(s);
          }
        }
        br.close();
      } catch (Exception e) {
        System.out.println("ERROR: Invalid input for REPL");
        System.out.println(e.getClass());

        for (StackTraceElement i : e.getStackTrace()) {
          System.out.println(i.toString());
        }

      }
    } else {
      System.out.println("ERROR: usage");
      System.out.print("./run --data=<list of files> \n[--prefix] [--whitespace] [--led=<led>]\n");
    }
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());
    FreeMarkerEngine freeMarker = createEngine();

    // Setup Spark Routes
    Spark.get("/autocorrect", new AutocorrectHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * A handler to produce our autocorrect service site.
   *
   * @return ModelAndView to render.
   * (autocorrect.ftl).
   */
  private static class AutocorrectHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title",
          "Autocorrect: Generate suggestions", "message", "Build your Autocorrector here!");
      return new ModelAndView(variables, "autocorrect.ftl");
    }
  }
}
