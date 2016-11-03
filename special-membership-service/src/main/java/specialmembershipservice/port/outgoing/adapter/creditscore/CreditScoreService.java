package specialmembershipservice.port.outgoing.adapter.creditscore;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class CreditScoreService {

    private final WebTarget webTarget;

    public CreditScoreService(WebTarget webTarget) {
        this.webTarget = checkNotNull(webTarget);
    }

    public Optional<Integer> lookup(String email) throws TemporarilyUnavailableException {
        try {
            CreditScoreDto creditScoreDto = webTarget.path("credit-scores").path(email)
                                                     .request().get(CreditScoreDto.class);
            return Optional.of(creditScoreDto.getCreditScore());
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (ServerErrorException e) {
            throw new TemporarilyUnavailableException(e);
        } catch (ProcessingException e) {
            if (e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException)
                throw new TemporarilyUnavailableException(e);
            throw e;
        }
    }
}
