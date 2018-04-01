package CI346.shurly;

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

import static spark.Spark.*;

@Slf4j
public class Shurly {

    private static Model model = null;

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

        Sql2o sql2o = new Sql2o(dbConnString, dbUser, dbPass);
        model = new Sql2oModel(sql2o);

        post("/", (req, res) -> {
                    String theURL = req.queryParams("the_url");
                    log.info("received POST: " + theURL);
                    final Map<String, Object> pageModel = new HashMap<>();
                    pageModel.put("HOME", HOME);
                    if (!isValidURL(theURL)) {
                        pageModel.put("ERROR", "INVALID_URL");
                    } else {
                        final String enc = Hashing.murmur3_32()
                                .hashString(theURL, StandardCharsets.UTF_8).toString();
                        String dupe = lookup(req.params(":enc"));
                        if(dupe.equals(Sql2oModel.URL_NOT_FOUND)) {
                            model.putURL(enc, theURL);
                        }
                        pageModel.put("URL", theURL);
                        pageModel.put("ENC", enc);
                    }
                    return render(pageModel, indexPath);
                });

        get("/", (req, res) -> {
            log.info("received GET");
            return render(new HashMap<>(), indexPath);
        });

        get("/:enc", (req, res) -> {
            String theURL = lookup(req.params(":enc"));
            if(theURL.equals(Sql2oModel.URL_NOT_FOUND)) {
                final Map<String, Object> pageModel = new HashMap<>();
                pageModel.put("ERROR", "URL_NOT_FOUND");
                return render(pageModel, indexPath);
            } else {
                res.redirect(theURL);
            }
            return "?";//There is presumably a better way of doing this
        });
    }

    private static boolean isValidURL(String url) {
        boolean valid = true;
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            valid = false;
        }
        return valid;
    }

    private static String lookup(String enc) {
        return model.getURL(enc);
    }

    private static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
