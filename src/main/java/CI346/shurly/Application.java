package CI346.shurly;
/**
 * The entry point for our application. Running the main method starts the server.
 */

import com.google.common.hash.Hashing;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static CI346.shurly.STATUS.URL_NOT_FOUND;
import static spark.Spark.*;

@Slf4j
public class Application {

    //Our Data Access Object (DAO)
    private static Model model = null;

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        //Read in the config
        Config conf         = ConfigFactory.load();
        int port            = conf.getInt("web.port");
        String host         = conf.getString("web.host");
        String HOME         = host +":"+port;
        String staticLoc    = conf.getString("web.staticFiles");
        String templatePath = conf.getString("web.templatePath");
        String indexPath    = templatePath + "index.vm";
        long staticTO       = conf.getLong("web.staticTimeout");
        String dbConnString = conf.getString("db.connectionString");
        String dbUser       = conf.getString("db.user");
        String dbPass       = conf.getString("db.password");

        // Configure Spark
        port(port);
        staticFiles.location(staticLoc);
        staticFiles.expireTime(staticTO);

        // Set up the DAO
        Sql2o sql2o = new Sql2o(dbConnString, dbUser, dbPass);
        model = new Sql2oModel(sql2o);

        // Define the routes

        // Handle POST submissions to the home page
        post("/", (req, res) -> {
            // get the form content that was submitted
            String theURL = req.queryParams("the_url");
            log.info("received POST: " + theURL);
            // set up the model for the template
            final Map<String, Object> pageModel = new HashMap<>();
            // store the URL for the homepage in the template model
            pageModel.put("HOME", HOME);
            if (!isValidURL(theURL)) {
                pageModel.put("ERROR", "INVALID_URL");
            } else {
                // encode the URL
                final String enc = Hashing.murmur3_32()
                                .hashString(theURL, StandardCharsets.UTF_8).toString();
                // look up the encoding to see if we stored it before
                ShurlyURL u = model.getURL(enc);
                if(u.getStatus().equals(URL_NOT_FOUND)) {
                    // store the encoding if it is new
                    model.putURL(enc, theURL);
                }
                // store the URL and its encoding in the template model
                pageModel.put("URL", theURL);
                pageModel.put("ENC", enc);
            }
            // render the index template with the appropriate model
            return render(pageModel, indexPath);
        });

        // Handle GET requests for the home page
        get("/", (req, res) -> {
            log.info("received GET");
            // render the index template with an empty model
            return render(new HashMap<>(), indexPath);
        });

        // Handle GET requests for a shortcut
        get("/:enc", (req, res) -> {
            // look up the encoding in the database
            ShurlyURL u = model.getURL(req.params(":enc"));
            if(u.getStatus().equals(URL_NOT_FOUND)) {
                // Not a real encoding, show the home page with an error message
                final Map<String, Object> pageModel = new HashMap<>();
                pageModel.put("ERROR", URL_NOT_FOUND.toString());
                return render(pageModel, indexPath);
            } else {
                // The encoding is in the database, so redirect to the URL
                res.redirect(u.getUrl());
            }
            return null;// There is presumably a better way of doing this...
        });
    }

    /**
     * Helper method to validate URLs
     * @param url
     * @return
     */
    private static boolean isValidURL(String url) {
        boolean valid = true;
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            valid = false;
        }
        return valid;
    }

    /**
     * Helper method to render a velocity template
     * @param model
     * @param templatePath
     * @return
     */
    private static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
