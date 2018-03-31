package CI346.shurly;

import lombok.Data;
import java.util.UUID;

@Data
public class ShurlyURL {
    private UUID post_uuid;
    private String url;
    private String enc;
}

