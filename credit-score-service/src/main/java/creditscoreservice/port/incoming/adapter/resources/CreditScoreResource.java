package creditscoreservice.port.incoming.adapter.resources;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import creditscoreservice.port.outgoing.adapter.creditscore.CreditScore;
import creditscoreservice.port.outgoing.adapter.creditscore.InMemoryCreditScoreRepository;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("/credit-scores/{email}")
public class CreditScoreResource {

  private final InMemoryCreditScoreRepository creditScoreRepository;

  public CreditScoreResource(InMemoryCreditScoreRepository creditScoreRepository) {
    this.creditScoreRepository = checkNotNull(creditScoreRepository);
  }

  @GET
  public Response get(@PathParam("email") String email) {
    Optional<CreditScore> possibleCreditScore = creditScoreRepository.lookup(email);
    if (possibleCreditScore.isEmpty()) {
      return Response.status(NOT_FOUND).build();
    }
    CreditScoreDto creditScoreDto = toDto(possibleCreditScore.get());
    return Response.ok(creditScoreDto).build();
  }

  @PUT
  public Response put(@PathParam("email") String email,
      @NotNull @Valid CreditScoreDto creditScoreDto) {
    CreditScore creditScore = toCreditScore(email, creditScoreDto);
    creditScoreRepository.save(creditScore);
    CreditScoreDto newCreditScoreDto = toDto(creditScore);
    return Response.ok(newCreditScoreDto).build();
  }

  private CreditScoreDto toDto(CreditScore creditScore) {
    CreditScoreDto dto = new CreditScoreDto();
    dto.setEmail(creditScore.getEmail());
    dto.setCreditScore(creditScore.getCreditScore());
    return dto;
  }

  private CreditScore toCreditScore(String email, CreditScoreDto dto) {
    return new CreditScore(email, dto.getCreditScore());
  }
}
