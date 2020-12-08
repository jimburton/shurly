package CI346.shurly;
/**
 * The entry point for our application. Running the main method starts the server.
 */

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static CI346.shurly.STATUS.*;
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

        // Handle POST submissions with Accept set to text/html
        post("/", "text/html", (req, res) -> {
            // get the form content that was submitted
            String theURL = req.queryParams("the_url");
            log.info("received POST with Accept: text/html: " + theURL);

            ShurlyURL u = handlePost(theURL);

            // set up the model for the template
            final Map<String, Object> pageModel = new HashMap<>();
            // store the URL for the homepage in the template model
            pageModel.put("HOME", HOME);
            if (u.getStatus().equals(INVALID_URL)) {
                pageModel.put("ERROR", INVALID_URL.toString());
            } else {
                // encode the URL
                final String enc = Hashing.murmur3_32()
                                .hashString(theURL, StandardCharsets.UTF_8).toString();
                // look up the encoding to see if we stored it before
                if(u.getStatus().equals(URL_NOT_FOUND)) {
                    // store the encoding if it is new
                    model.putURL(enc, theURL);
                }
                // store the URL and its encoding in the template model
                pageModel.put("URL", theURL);
                pageModel.put("ENC", u.getEnc());
            }
            // render the index template with the appropriate model
            return render(pageModel, indexPath);
        });

        // Handle POST submissions with Accept set to application/json
        post("/", "application/json", (req, res) -> {
            res.type("application/json");
            // get the form content that was submitted
            String theURL = req.queryParams("the_url");
            log.info("received POST with Accept: application/json: " + theURL);

            ShurlyURL u = handlePost(theURL);
            // render the index template with the appropriate model
            return new Gson().toJson(u);
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

    private static ShurlyURL handlePost(String theURL) {
        ShurlyURL u = new ShurlyURL();
        if (!isValidURL(theURL)) {
            u.setStatus(INVALID_URL);
        } else {
            // encode the URL
            final String enc = Hashing.murmur3_32()
                    .hashString(theURL, StandardCharsets.UTF_8).toString();
            // look up the encoding to see if we stored it before
            u = model.getURL(enc);
            if(u.getStatus().equals(URL_NOT_FOUND)) {
                // store the encoding if it is new
                model.putURL(enc, theURL);
                u.setStatus(SUCCESS);
            }
            // store the URL and its encoding in the POJO
            u.setUrl(theURL);
            u.setEnc(enc);
        }
        return u;
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
