package welcomememberemailservice.port.incoming.adapter.kafka;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Email;

@JsonTypeName("memberSignedUpEvent")
public class MemberSignedUpEvent extends Event {

  @Email
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
