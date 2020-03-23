package specialmembershipservice.bootstrap;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.time.ZonedDateTime;
import org.junit.Test;

public class ObjectMapperConfigTest {

  private final ObjectMapper objectMapper = ObjectMapperConfig.applyTo(Jackson.newObjectMapper());

  @Test
  public void forwardCompatibility() {
    String dto = "{\"notRecognizableField\":\"value\"}";
    try {
      objectMapper.readValue(dto, BlankDto.class);
    } catch (IOException e) {
      throw new AssertionError("Should not fail de-serializing unknown fields", e);
    }
  }

  @Test
  public void camelCase() throws Exception {
    CamelCasedDto dto = new CamelCasedDto();
    dto.twoWords = "value";
    assertThat(objectMapper.writeValueAsString(dto), equalTo("{\"twoWords\":\"value\"}"));
  }

  @Test
  public void iso8601Timestamps() throws IOException {
    TimestampedDto dto = new TimestampedDto();
    dto.timestamp = ZonedDateTime.of(2019, 12, 1, 16, 30, 0, 0, UTC);
    assertThat(objectMapper.writeValueAsString(dto),
        equalTo("{\"timestamp\":\"2019-12-01T16:30:00Z\"}"));
  }

  private static class BlankDto {

  }

  private static class CamelCasedDto {

    public String twoWords;
  }

  private static class TimestampedDto {

    public ZonedDateTime timestamp;
  }
}