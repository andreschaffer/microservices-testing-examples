package specialmembershipservice.bootstrap;

import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public class SpecialMembershipServiceConfiguration extends Configuration {

  @URL
  private String creditScoreServiceUrl;

  @NotNull
  @Valid
  private EventPublisherConfiguration eventPublisher;

  public String getCreditScoreServiceUrl() {
    return creditScoreServiceUrl;
  }

  public void setCreditScoreServiceUrl(String creditScoreServiceUrl) {
    this.creditScoreServiceUrl = creditScoreServiceUrl;
  }

  public EventPublisherConfiguration getEventPublisher() {
    return eventPublisher;
  }

  public void setEventPublisher(EventPublisherConfiguration eventPublisher) {
    this.eventPublisher = eventPublisher;
  }
}
