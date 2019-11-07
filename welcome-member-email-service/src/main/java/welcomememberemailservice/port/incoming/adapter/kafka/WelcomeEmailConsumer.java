package welcomememberemailservice.port.incoming.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.lifecycle.Managed;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import welcomememberemailservice.port.outgoing.adapter.email.SmtpEmailSender;
import welcomememberemailservice.port.outgoing.adapter.email.SmtpEmailSenderException;

import javax.validation.Validator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class WelcomeEmailConsumer implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(WelcomeEmailConsumer.class);
    private final String topic;
    private final KafkaConsumer<String, String> consumer;
    private final MessageParser messageParser;
    private final SmtpEmailSender emailSender;
    private final AtomicBoolean stop;

    public WelcomeEmailConsumer(String topic, Map<String, Object> configs, ObjectMapper objectMapper,
                                Validator validator, SmtpEmailSender emailSender) {
        this.topic = checkNotNull(topic);
        this.consumer = new KafkaConsumer<>(checkNotNull(configs));
        this.messageParser = new MessageParser(objectMapper, validator);
        this.emailSender = checkNotNull(emailSender);
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void start() throws Exception {
        Executors.newFixedThreadPool(1).execute(this::subscribeConsumer);
    }

    private void subscribeConsumer() {
        consumer.subscribe(singletonList(topic));
        LOG.info("Subscribed consumer to topic {}", topic);

        acceptMessages();
    }

    private void acceptMessages() {
        while (!stop.get()) {
            ConsumerRecords<String, String> records;
            try {
                records = consumer.poll(1000);
            } catch (WakeupException e) {
                if (!stop.get()) throw e;
                break;
            }
            for (ConsumerRecord<String, String> record : records) {
                acceptMessage(record);
                commitOffset(record);
            }
        }
    }

    private void acceptMessage(ConsumerRecord<String, String> record) throws SmtpEmailSenderException {
        try {
            Event event = messageParser.parse(record.value(), Event.class);
            if (event instanceof MemberSignedUpEvent) {
                LOG.info("Accepted {}", record.value());
                emailSender.send(((MemberSignedUpEvent) event).getEmail());
            } else {
                LOG.info("Not a type we are interested in: {}. Ignoring and continuing.", event.getType());
            }
        } catch (InvalidMessageException e) {
            LOG.warn("Invalid message: {}. Ignoring and continuing.", record.value(), e);
        }
    }

    private void commitOffset(ConsumerRecord<String, String> record) {
        long offsetToCommit = record.offset() + 1;
        consumer.commitSync(singletonMap(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(offsetToCommit)));
    }

    @Override
    public void stop() throws Exception {
        stop.set(true);
        consumer.wakeup();
    }
}
