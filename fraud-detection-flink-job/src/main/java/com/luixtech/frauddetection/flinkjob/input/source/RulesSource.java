package com.luixtech.frauddetection.flinkjob.input.source;

import com.luixtech.frauddetection.flinkjob.dynamicrules.Rule;
import com.luixtech.frauddetection.flinkjob.dynamicrules.functions.RuleDeserializer;
import com.luixtech.frauddetection.flinkjob.input.InputConfig;
import com.luixtech.frauddetection.flinkjob.input.Parameters;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.luixtech.frauddetection.flinkjob.input.Parameters.RULES_SOURCE;
import static com.luixtech.frauddetection.flinkjob.input.SourceUtils.getKafkaSource;

public class RulesSource {

    public static RulesSource.Type getRulesSourceType(InputConfig inputConfig) {
        String rulesSource = inputConfig.get(RULES_SOURCE);
        return RulesSource.Type.valueOf(rulesSource.toUpperCase());
    }

    public static DataStreamSource<String> initRulesSource(InputConfig inputConfig, StreamExecutionEnvironment env) throws IOException {
        RulesSource.Type rulesSourceType = getRulesSourceType(inputConfig);
        DataStreamSource<String> dataStreamSource;

        switch (rulesSourceType) {
            case KAFKA:
                // Specify the topic from which the rules are read
                String rulesTopic = inputConfig.get(Parameters.RULES_TOPIC);
                KafkaSource<String> kafkaSource = getKafkaSource(inputConfig, rulesTopic);

                // NOTE: Idiomatically, watermarks should be assigned here, but this done later
                // because of the mix of the new Source (Kafka) and SourceFunction-based interfaces.
                // TODO: refactor when FLIP-238 is added
                dataStreamSource =
                        env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), rulesSourceType.getName());
                break;
            case SOCKET:
                SocketTextStreamFunction socketSourceFunction =
                        new SocketTextStreamFunction("localhost", inputConfig.get(Parameters.SOCKET_PORT), "\n", -1);
                dataStreamSource = env.addSource(socketSourceFunction);
                break;
            default:
                throw new IllegalArgumentException(
                        "Source \"" + rulesSourceType + "\" unknown. Known values are:" + Arrays.toString(Type.values()));
        }
        return dataStreamSource;
    }

    public static DataStream<Rule> stringsStreamToRules(DataStream<String> ruleStrings) {
        return ruleStrings
                .flatMap(new RuleDeserializer())
                .name("Rule Deserialization")
//                .setParallelism(RULES_STREAM_PARALLELISM)
                .assignTimestampsAndWatermarks(
                        new BoundedOutOfOrdernessTimestampExtractor<Rule>(Time.of(0, TimeUnit.MILLISECONDS)) {
                            @Override
                            public long extractTimestamp(Rule element) {
                                // Prevents connected data+update stream watermark stalling.
                                return Long.MAX_VALUE;
                            }
                        });
    }

    public enum Type {
        KAFKA("Rules Source (Kafka)"),
        PUBSUB("Rules Source (Pub/Sub)"),
        SOCKET("Rules Source (Socket)");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}