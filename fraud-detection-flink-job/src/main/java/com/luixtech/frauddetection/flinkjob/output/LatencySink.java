package com.luixtech.frauddetection.flinkjob.output;

import com.luixtech.frauddetection.flinkjob.input.Arguments;
import com.luixtech.frauddetection.flinkjob.utils.KafkaPropertyUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

import java.util.Properties;

public class LatencySink {

    public static DataStreamSink<String> addLatencySink(Arguments arguments, DataStream<String> stream) {
        DataStreamSink<String> dataStreamSink;

        switch (arguments.messageChannel) {
            case "kafka":
                Properties kafkaProps = KafkaPropertyUtils.initProducerProperties(arguments);
                KafkaSink<String> kafkaSink =
                        KafkaSink.<String>builder()
                                .setKafkaProducerConfig(kafkaProps)
                                .setRecordSerializer(
                                        KafkaRecordSerializationSchema.builder()
                                                .setTopic(arguments.latencyTopic)
                                                .setValueSerializationSchema(new SimpleStringSchema())
                                                .build())
                                .setDeliverGuarantee(DeliveryGuarantee.NONE)
                                .build();
                dataStreamSink = stream.sinkTo(kafkaSink);
                break;
            case "socket":
                dataStreamSink = stream.addSink(new PrintSinkFunction<>(true));
                break;
            default:
                throw new IllegalArgumentException("Invalid latency sink " + arguments.messageChannel);
        }
        return dataStreamSink;
    }
}
