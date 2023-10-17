package specialmembershipservice.port.incoming.adapter.resources;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import specialmembershipservice.port.outgoing.adapter.creditscore.CreditScoreDto;
import specialmembershipservice.port.outgoing.adapter.creditscore.CreditScoreService;
import specialmembershipservice.port.outgoing.adapter.creditscore.TemporarilyUnavailableException;
import specialmembershipservice.port.outgoing.adapter.eventpublisher.EventPublisher;
import specialmembershipservice.port.outgoing.adapter.eventpublisher.MemberSignedUpEvent;

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("/special-memberships")
public class SpecialMembershipsResource {

  private static final int MIN_ACCEPTED_CREDIT_SCORE = 700;
  private final CreditScoreService creditScoreService;
  private final EventPublisher eventPublisher;

  public SpecialMembershipsResource(CreditScoreService creditScoreService,
      EventPublisher eventPublisher) {
    this.creditScoreService = checkNotNull(creditScoreService);
    this.eventPublisher = checkNotNull(eventPublisher);
  }

  @POST
  public Response post(@NotNull @Valid SpecialMembershipDto specialMembershipDto) {
    Optional<CreditScoreDto> possibleCreditScore;
    try {
      possibleCreditScore = creditScoreService.lookup(specialMembershipDto.getEmail());
    } catch (TemporarilyUnavailableException e) {
      return Response.status(SERVICE_UNAVAILABLE).build();
    }
    if (possibleCreditScore.isEmpty()
        || possibleCreditScore.get().getCreditScore() < MIN_ACCEPTED_CREDIT_SCORE) {
      return Response.status(FORBIDDEN).build();
    }

    eventPublisher.publish(new MemberSignedUpEvent(specialMembershipDto.getEmail(), now(UTC)));
    return Response.ok().build();
  }
}
