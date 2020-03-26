package specialmembershipservice.port.outgoing.adapter.creditscore;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;

public class CreditScoreService {

  private final WebTarget webTarget;

  public CreditScoreService(WebTarget webTarget) {
    this.webTarget = checkNotNull(webTarget);
  }

  public Optional<CreditScoreDto> lookup(String email) throws TemporarilyUnavailableException {
    try {
      CreditScoreDto creditScoreDto = webTarget.path("credit-scores").path(email)
          .request().get(CreditScoreDto.class);
      return Optional.of(creditScoreDto);
    } catch (NotFoundException e) {
      return Optional.empty();
    } catch (RuntimeException e) {
      if (isServerRelated(e) || isNetworkRelated(e)) {
        throw new TemporarilyUnavailableException(e);
      }
      throw e;
    }
  }

  private boolean isServerRelated(RuntimeException e) {
    return e instanceof ServerErrorException;
  }

  private boolean isNetworkRelated(RuntimeException e) {
    return e instanceof ProcessingException
        &&
        (e.getCause() instanceof ConnectException
            || e.getCause() instanceof SocketTimeoutException);
  }
}
