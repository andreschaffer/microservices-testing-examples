package welcomememberemailservice.it.kafka;

import kafka.common.OffsetMetadataAndError;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetFetchRequest;
import kafka.javaapi.OffsetFetchResponse;
import kafka.network.BlockingChannel;

import java.time.Duration;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;

public class KafkaOffsets {

    private static final Random RANDOM = new Random();
    private final String host;
    private final Integer port;

    public KafkaOffsets(String host, Integer port) {
        this.host = checkNotNull(host);
        this.port = checkNotNull(port);
    }

    public long readOffset(String topic, String groupId) {
        return readOffset(topic, 0, groupId);
    }

    public long readOffset(String topic, int partition, String groupId) {
        BlockingChannel channel = new BlockingChannel(host, port,
                BlockingChannel.UseDefaultBufferSize(),
                BlockingChannel.UseDefaultBufferSize(),
                (int) Duration.ofMillis(100).toMillis());
        try {
            channel.connect();
            TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
            OffsetFetchRequest offsetFetchRequest = new OffsetFetchRequest(
                    groupId, singletonList(topicAndPartition), (short) 1, RANDOM.nextInt(), "test-checker");
            channel.send(offsetFetchRequest.underlying());
            OffsetFetchResponse fetchResponse = OffsetFetchResponse.readFrom(channel.receive().payload());
            OffsetMetadataAndError result = fetchResponse.offsets().get(topicAndPartition);
            return result.offset();
        } finally {
            channel.disconnect();
        }
    }
}
