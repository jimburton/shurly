package CI346.shurly;
/**
 * A POJO to hold records from the database. The @Data annotation
 * saves us from having to write getters and setters.
 */

import lombok.Data;
import java.util.UUID;

@Data
public class ShurlyURL {
    private UUID post_uuid;
    private String url;
    private String enc;
}

