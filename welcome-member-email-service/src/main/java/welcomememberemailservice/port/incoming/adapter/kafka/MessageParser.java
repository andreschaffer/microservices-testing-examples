package welcomememberemailservice.port.incoming.adapter.kafka;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class MessageParser {

  private final ObjectMapper objectMapper;
  private final Validator validator;

  public MessageParser(ObjectMapper objectMapper, Validator validator) {
    this.objectMapper = checkNotNull(objectMapper).copy();
    this.validator = checkNotNull(validator);
  }

  public <T> T parse(String message, Class<T> clazz) throws InvalidMessageException {
    T event;
    try {
      event = objectMapper.readValue(message, clazz);
    } catch (IOException e) {
      throw new InvalidMessageException(e);
    }

    Set<ConstraintViolation<T>> constraintViolations = validator.validate(event);
    if (!constraintViolations.isEmpty()) {
      throw new InvalidMessageException(new ConstraintViolationException(constraintViolations));
    }
    return event;
  }
}
