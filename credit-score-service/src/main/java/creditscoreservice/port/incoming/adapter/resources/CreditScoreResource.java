package creditscoreservice.port.incoming.adapter.resources;

import creditscoreservice.port.outgoing.adapter.creditscore.CreditScore;
import creditscoreservice.port.outgoing.adapter.creditscore.InMemoryCreditScoreRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

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
        if (possibleCreditScore.isEmpty()) return Response.status(NOT_FOUND).build();
        CreditScoreDto creditScoreDto = toDto(possibleCreditScore.get());
        return Response.ok(creditScoreDto).build();
    }

    @PUT
    public Response put(@PathParam("email") String email, @NotNull @Valid CreditScoreDto creditScoreDto) {
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
