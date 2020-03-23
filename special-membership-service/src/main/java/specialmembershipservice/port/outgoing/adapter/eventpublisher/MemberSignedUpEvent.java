package specialmembershipservice.port.outgoing.adapter.eventpublisher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.ZonedDateTime;

public class MemberSignedUpEvent extends Event {

  private final String email;

  public MemberSignedUpEvent(String email, ZonedDateTime timestamp) {
    super("memberSignedUpEvent", timestamp);
    this.email = checkNotNull(email);
  }

  public String getEmail() {
    return email;
  }
}
