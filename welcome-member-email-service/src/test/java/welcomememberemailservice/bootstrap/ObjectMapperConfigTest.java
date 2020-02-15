package welcomememberemailservice.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ObjectMapperConfigTest {

    private final ObjectMapper objectMapper = ObjectMapperConfig.applyTo(Jackson.newObjectMapper());

    @Test
    public void forwardCompatibility() {
        String dto = "{\"notRecognizableField\":\"value\"}";
        try {
            objectMapper.readValue(dto, BlankDto.class);
        } catch (IOException e) {
            fail("Should not fail de-serializing unknown fields");
        }
    }

    @Test
    public void camelCase() throws Exception {
        CamelCasedDto dto = new CamelCasedDto();
        dto.twoWords = "value";
        assertThat(objectMapper.writeValueAsString(dto), equalTo("{\"twoWords\":\"value\"}"));
    }

    private static class BlankDto {}

    private static class CamelCasedDto {
        public String twoWords;
    }
}