package specialmembershipservice.port.outgoing.adapter.eventpublisher;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public abstract class Event {

  @JsonProperty("@type")
  private final String type;
  private final ZonedDateTime timestamp;

  public Event(String type, ZonedDateTime timestamp) {
    this.type = checkNotNull(type);
    this.timestamp = checkNotNull(timestamp);
  }

  public String getType() {
    return type;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }
}
