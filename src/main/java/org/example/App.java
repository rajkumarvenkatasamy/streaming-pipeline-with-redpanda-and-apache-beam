package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class App {

    private static final Logger LOG = LoggerFactory
            .getLogger(App.class);


    /**
     * Specific pipeline options.
     */
    public interface Options extends PipelineOptions {
        @Description("Kafka Bootstrap Servers")
        @Default.String("localhost:9092")
        String getKafkaServer();

        void setKafkaServer(String value);

        @Description("Kafka Input Topic Name")
        @Default.String("user_activity")
        String getInputTopic();

        void setInputTopic(String value);

        @Description("Kafka Output Topic Name")
        @Default.String("events_from_south")
        String getOutputTopic();

        void setOutputTopic(String value);

        @Description("Duration to wait in seconds")
        @Default.Long(-1)
        Long getDuration();

        void setDuration(Long duration);

    }

    private static EnrichedUserActivity filterAndEnrichEvents(String row) throws JsonProcessingException {
        StateAndRegion stateAndRegion = new StateAndRegion();
        EnrichedUserActivity enrichedUserActivity = new EnrichedUserActivity();
        ObjectMapper objectMapper = new ObjectMapper();
        UserActivity userActivity = objectMapper.readValue(row, UserActivity.class);
        if (stateAndRegion.getRegionByStateCode(userActivity.getStateCode()).equals("South")) {
            System.out.println("South");
            enrichedUserActivity.setUserId(userActivity.getUserId());
            enrichedUserActivity.setProductId(userActivity.getProductId());
            enrichedUserActivity.setStateDescription(stateAndRegion.getStateDescriptionByStateCode(
                    userActivity.getStateCode()));
            enrichedUserActivity.setRegion("South");
        }
        System.out.println("enrichedUserActivity is : " + enrichedUserActivity);
        return enrichedUserActivity;
    }

    public static void main(String[] args) throws Exception {
        Options options = PipelineOptionsFactory.fromArgs(args)
                .withValidation().as(Options.class);
        LOG.info("Pipeline options are: ");
        LOG.info(options.toString());
        LOG.info("Printed Pipeline options");
        Pipeline pipeline = Pipeline.create(options);

        // now we connect to the queue and process every event
        PCollection<String> data = pipeline.apply(
                "ReadFromKafka",
                KafkaIO.<String, String> read()
                        .withBootstrapServers(options.getKafkaServer())
                        .withTopics(
                                Collections.singletonList(options
                                        .getInputTopic()))
                        .withKeyDeserializer(StringDeserializer.class)
                        .withValueDeserializer(StringDeserializer.class)
                        .withoutMetadata()).apply("ExtractPayload",
                Values.<String> create());

        data.apply(ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                System.out.println(String.format("** element |%s| **",
                        c.element()));
            }
        }));

        // We first filter the events coming from the states that belong to Southern region and
        // enrich the event information by transforming the state code to state description.
        // Finally, write the Southern region events to its own Topic

        PCollection<String> enrichedAndSegregatedEvents = data.apply("Filter and Enrich Event Information",
                ParDo.of(new DoFn<String, String>() {
                    @ProcessElement
                    public void processElement(ProcessContext c) throws JsonProcessingException {
                        EnrichedUserActivity enrichedUserActivity = filterAndEnrichEvents(c.element());
                        if (enrichedUserActivity.getUserId() != null) {
                            c.output(enrichedUserActivity.toString());
                        }
                    }
                }));

        PCollection<KV<String, String>> eventsKV = enrichedAndSegregatedEvents
                .apply("Prepare Events for the Output Topic",
                        ParDo.of(new DoFn<String, KV<String, String>>() {
                            @ProcessElement
                            public void processElement(ProcessContext c)
                                    throws Exception {
                                /*
                                System.out.println("c.element is : " + c.element());
                                System.out.println("c.element KV is : " + KV.of("South", c.element()));
                                System.out.println("c.element KV is : " + KV.of("region", c.element()));
                                */
                                c.output(KV.of("South", c.element()));
                            }
                        }));

        eventsKV
                .apply("WriteToKafka",
                        KafkaIO.<String, String> write()
                                .withBootstrapServers(
                                        options.getKafkaServer())
                                .withTopic(options.getOutputTopic())
                                .withKeySerializer(
                                        org.apache.kafka.common.serialization.StringSerializer.class)
                                .withValueSerializer(
                                        org.apache.kafka.common.serialization.StringSerializer.class));

        PipelineResult run = pipeline.run();
        run.waitUntilFinish(Duration.standardSeconds(options.getDuration()));
    }
}