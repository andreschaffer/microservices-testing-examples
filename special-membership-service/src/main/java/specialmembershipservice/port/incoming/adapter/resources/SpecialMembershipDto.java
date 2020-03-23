package specialmembershipservice.port.incoming.adapter.resources;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class SpecialMembershipDto {

  @NotNull
  @Email
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
