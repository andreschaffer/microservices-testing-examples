package specialmembershipservice.it.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static javax.ws.rs.client.Entity.json;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.glassfish.jersey.logging.LoggingFeature.DEFAULT_LOGGER_NAME;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
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

  public Response postSpecialMembership(Map<String, Object> specialMembershipDto) {
    return client.target(resourcesUrls.specialMembershipUrl()).request()
        .post(json(specialMembershipDto));
  }
}
