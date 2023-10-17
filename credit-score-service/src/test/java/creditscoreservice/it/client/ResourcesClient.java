package creditscoreservice.it.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.client.Entity.json;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.glassfish.jersey.logging.LoggingFeature.DEFAULT_LOGGER_NAME;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.glassfish.jersey.logging.LoggingFeature;

public class ResourcesClient {

  private final Client client;
  private final ResourcesUrls resourcesUrls;

  public ResourcesClient(Environment environment, int port) {
    this.client = new JerseyClientBuilder(checkNotNull(environment))
        .build(ResourcesClient.class.getName())
        .property(CONNECT_TIMEOUT, 2000)
        .property(READ_TIMEOUT, 10000)
        .register(new LoggingFeature(getLogger(DEFAULT_LOGGER_NAME), INFO, PAYLOAD_ANY, 1024));
    this.resourcesUrls = new ResourcesUrls(port);
  }

  public ResourcesUrls getResourcesUrls() {
    return resourcesUrls;
  }

  public Response putCreditScore(String email, Map<String, Object> creditScoreDto) {
    return client.target(resourcesUrls.creditScoreUrl(email)).request().put(json(creditScoreDto));
  }

  public Response getCreditScore(String email) {
    return client.target(resourcesUrls.creditScoreUrl(email)).request().get();
  }
}
