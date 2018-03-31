package CI346.shurly;

import com.google.common.hash.Hashing;
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

    static Model model = null;

    public static void main(String[] args) {
        // Configure Spark
        port(4567);
        staticFiles.location("/public");
        staticFiles.expireTime(600L);

        Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/shurly",
                "shurly", "shurly");
        model = new Sql2oModel(sql2o);
        Map<String, Object> pageModel = new HashMap<>();
        pageModel.put("HOME", "http://localhost:4567");

        post("/", (req, res) -> {
                    String theURL = req.queryParams("the_url");
                    log.info("received POST: " + theURL);
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
                        pageModel.remove("ERROR");
                    }
                    return render(pageModel, "templates/index.vm");
                });

        get("/", (req, res) -> {
            log.info("received GET");
            return render(pageModel, "templates/index.vm");
        });

        get("/:enc", (req, res) -> {
            String theURL = lookup(req.params(":enc"));
            if(theURL.equals(Sql2oModel.URL_NOT_FOUND)) {
                pageModel.put("ERROR", "URL_NOT_FOUND");
                return render(pageModel, "templates/index.vm");
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

    public static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
