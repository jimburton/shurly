package CI346.shurly;
/**
 * Our Data Access Object (DAO). The constructor is supplied with an
 * instance of Sql20 which has been configured (in the Shurly class)
 * to connect to a MySQL database running on localhost.
 */

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

public class Sql2oModel implements Model {

    private Sql2o sql2o;
    public final static String URL_NOT_FOUND = "URL_NOT_FOUND";

    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    /**
     * Put a new URL/encoding pair in the database
     * @param enc
     * @param url
     */
    @Override
    public void putURL(String enc, String url) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery("INSERT INTO urls(enc, url) " +
                    "VALUES (:enc, :url)")
                    .addParameter("enc", enc)
                    .addParameter("url", url)
                    .executeUpdate();
            conn.commit();
        }
    }

    /**
     * Retrieve a URL given its encoding
     * @param enc
     * @return
     */
    @Override
    public String getURL(String enc) {
        try (Connection conn = sql2o.open()) {
            List<ShurlyURL> result = conn.createQuery(
                    "SELECT url FROM urls WHERE enc = :enc")
                    .addParameter("enc", enc)
                    .executeAndFetch(ShurlyURL.class);
            return (result.size() == 1) ? result.get(0).getUrl() : URL_NOT_FOUND;
        }
    }
}
