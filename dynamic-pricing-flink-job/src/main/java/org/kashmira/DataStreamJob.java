package org.kashmira;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.util.Collector;
import org.kashmira.dto.event.Event;
import org.kashmira.dto.pricing.PricingOutput;
import org.kashmira.dto.pricing.PricingOutputSerializer;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DataStreamJob {

    public static void main(String[] args) throws Exception {

        final int BASE_PRICE = 100;
        final String KAFKA_BOOTSTRAP_SERVER = "localhost:9092";
        final String GROUP_ID = "group-id";
        final String SINK_TOPIC = "dynamicpricing.output";
        List<String> SOURCE_TOPICS = List.of("dynamicpricing.update");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        ObjectMapper mapper = new ObjectMapper();

        KafkaSource<ObjectNode> source = KafkaSource.<ObjectNode>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVER)
                .setGroupId(GROUP_ID)
                .setTopics(SOURCE_TOPICS)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaRecordDeserializationSchema.of(new JSONKeyValueDeserializationSchema(false)))
                .setClientIdPrefix(UUID.randomUUID().toString())
                .build();

        /*
         * Approach:- parse timestamp from string
         * */

        DataStreamSource<ObjectNode> dataStreamSource = env.fromSource(source, WatermarkStrategy
                .<ObjectNode>forMonotonousTimestamps()
                .withTimestampAssigner((event, timestamp) -> {
                    JsonNode timestampNode = event.get("value").get("timestamp");
                    if (timestampNode != null && timestampNode.isTextual()) {
                        try {
                            return Instant.parse(timestampNode.asText()).toEpochMilli();
                        } catch (DateTimeParseException e) {
                            System.err.println("Invalid timestamp format: " + timestampNode.asText());
                        }
                    }
                    return System.currentTimeMillis();
                }), "Kafka Source for Dynamic Pricing Flink JOB");


        dataStreamSource.print("Raw Kafka Data");

        /*
         * We will use Jackson's objectNode and map it to convert the parsed value to event objects to get our input
         * EventStream
         * */

        DataStream<Event> eventStream = dataStreamSource.map(objectNode -> {
            JsonNode valueNode = objectNode.get("value");
            return mapper.treeToValue(valueNode, Event.class);
        });

        /*
         * To impart some flexibility, we will use a watermark that stays 30 seconds behind the last-processed
         * event to account for high traffic volume and bandwidth and compute latency, keeping load fluctuations in mind.
         * */

        eventStream.assignTimestampsAndWatermarks(WatermarkStrategy
                .<Event>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                .withTimestampAssigner((event, timestamp) -> Instant.parse(event.getTimestamp()).toEpochMilli()));

        eventStream.print("Mapped Events");

        /*
         * Overall approach for stream transformation
         * 1 - Key by property ID. This will give take our Datastream and transform it into a Keyedstream
         * and group our events by propertyID. This will allow us to compute events on a per-property basis
         * and apply dynamic prices based on distinct users and eventTypes. Different evetTypes will have
         * a different impact on the price. Bookings for example will have more of a impact as views.
         *
         * 2 - Now we will iterate over events in each group, dedupe them based on userID and eventType to
         * eliminate noise. In our strategy, we will only consider a userID and eventType combination once to
         * eliminate noise
         *
         * 3 - After deduping, we finally compute the prices on a per-property basis and send the updated
         * price object to our kafka consumer.
         *
         * Improvements :-
         * 1 Rather than doing two keyBys on the same attribute, we can do a single dedup
         * followed by a single keyBy in the future.
         * 2 Add an exponential decay function for smoother decay based on the position of last-seen event relative
         * to the current watermark for more competetive dynamic pricing and fix abrupts price drops.
         * */
        KeyedStream<Event, String> keyedByPropertyStream = eventStream
                .keyBy(Event::getPropertyID); //todo: optimize further by deduping before keyBy for more efficient resource usage. This will do for now

        SingleOutputStreamOperator<Event> deduplicatedStream = keyedByPropertyStream
                .window(SlidingEventTimeWindows.of(Duration.ofMinutes(30), Duration.ofMinutes(1)))  // Adjusted window size
                .apply(new WindowFunction<Event, Event, String, TimeWindow>() {
                    @Override
                    public void apply(String key, TimeWindow window, Iterable<Event> events, Collector<Event> out) {
                        System.out.println("Events entering" + events);
                        Set<String> uniqueUserEventTypes = new HashSet<>();
                        for (Event event : events) {
                            System.out.println("Incoming event" + event);
                            String uniqueKey = event.getUserID() + "_" + event.getEventType();
                            if (uniqueUserEventTypes.add(uniqueKey)) {
                                out.collect(event);
                            }
                        }
                    }
                });

        deduplicatedStream.print("Deduplicated Events");

        SingleOutputStreamOperator<PricingOutput> pricingStream = deduplicatedStream
                .keyBy(Event::getPropertyID)
                .window(SlidingEventTimeWindows.of(Duration.ofMinutes(30), Duration.ofMinutes(1)))  // Adjusted window size
                .apply(new WindowFunction<Event, PricingOutput, String, TimeWindow>() {
                    @Override
                    public void apply(String key, TimeWindow window, Iterable<Event> events, Collector<PricingOutput> out) {
                        double bookingImpact = 1.0;
                        double viewImpact = 1.0;

                        for (Event event : events) {
                            if ("booking".equals(event.getEventType())) { //todo: Convert to enum and use a map for other events, when need arises
                                bookingImpact += 0.1;
                            } else if ("view".equals(event.getEventType())) {
                                viewImpact += 0.05;
                            }
                        }

                        double adjustedPrice = (double) BASE_PRICE * bookingImpact * viewImpact;
                        out.collect(new PricingOutput(key, "adjustedPrice", adjustedPrice));
                    }
                });

        pricingStream.print("Pricing Adjustments");

        /*
         * 1 - Using a Kafka sink, we will be using Redis with TTLs to decide till whe the dynamic price should be applied and when the impact should be removed.
         * This is only a stop-gap solution, however.
         * 2 - Since Redis has issues with durability and is expensive to scale as an in-memory solution, a better approach would be to use a write-optimized database
         * like Cassandra for heavy writes with a write-behind redis cache with redis pub/sub functionality to balance out read performance.
         * 3 - Data freshness would be compromised with this trade-off, but dynamic pricing can accommodate this.
         * 4 -Data freshness would be a tunable parameter here based on more resources, we could reduce the syncing period accordingly.
         * */
        KafkaSink<PricingOutput> sink = KafkaSink.<PricingOutput>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVER)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(SINK_TOPIC)
                        .setValueSerializationSchema(new PricingOutputSerializer())
                        .build())
                .build();

        pricingStream.sinkTo(sink);

        env.execute("Flink Multi-Keying Dynamic Pricing Job");
    }

}
