package com.luixtech.frauddetection.flinkjob.serializer;

import com.luixtech.frauddetection.common.dto.Rule;
import com.luixtech.frauddetection.common.rule.RuleState;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

@Slf4j
public class RuleDeserializer extends RichFlatMapFunction<String, Rule> {

    private RuleParser ruleParser;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ruleParser = new RuleParser();
    }

    @Override
    public void flatMap(String value, Collector<Rule> out) {
        log.info("Received rule {}", value);
        try {
            Rule rule = ruleParser.fromString(value);
            if (rule.getRuleState() != RuleState.CONTROL && rule.getRuleId() == null) {
                throw new NullPointerException("Rule ID cannot be null");
            }
            out.collect(rule);
        } catch (Exception e) {
            log.warn("Failed to parse rule", e);
        }
    }
}
