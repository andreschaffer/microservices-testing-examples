package creditscoreservice.it.client;

import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;
import static javax.ws.rs.core.UriBuilder.fromUri;

public class ResourcesUrls {

    private int port;

    public ResourcesUrls(int port) {
        checkArgument(port > 0);
        this.port = port;
    }

    public URI creditScoreUrl(String email) {
        return fromUri("http://localhost").port(port).path("credit-scores").path(email).build();
    }
}
