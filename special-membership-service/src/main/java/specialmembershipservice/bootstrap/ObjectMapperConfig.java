package specialmembershipservice.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

class ObjectMapperConfig {

    static ObjectMapper applyTo(ObjectMapper objectMapper) {
        return objectMapper
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(WRITE_DATES_AS_TIMESTAMPS);
    }
}
