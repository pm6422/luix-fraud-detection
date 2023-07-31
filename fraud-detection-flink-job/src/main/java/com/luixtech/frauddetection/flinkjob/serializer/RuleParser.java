package com.luixtech.frauddetection.flinkjob.serializer;

import com.luixtech.frauddetection.common.dto.Rule;
import com.luixtech.frauddetection.common.dto.Rule.AggregatorFunctionType;
import com.luixtech.frauddetection.common.dto.Rule.LimitOperatorType;
import com.luixtech.frauddetection.common.rule.RuleState;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RuleParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Rule fromString(String line) throws IOException {
        if (!line.isEmpty() && line.startsWith("{") && line.endsWith("}")) {
            return parseJson(line);
        } else {
            return parsePlain(line);
        }
    }

    private Rule parseJson(String ruleString) throws IOException {
        return OBJECT_MAPPER.readValue(ruleString, Rule.class);
    }

    private static Rule parsePlain(String ruleString) throws IOException {
        List<String> tokens = Arrays.asList(ruleString.split(","));
        if (tokens.size() != 9) {
            throw new IOException("Invalid rule (wrong number of tokens): " + ruleString);
        }

        Iterator<String> iter = tokens.iterator();
        Rule rule = new Rule();

        rule.setRuleId(Integer.parseInt(stripBrackets(iter.next())));
        rule.setRuleState(RuleState.valueOf(stripBrackets(iter.next()).toUpperCase()));
        rule.setGroupingKeyNames(getNames(iter.next()));
        rule.setUnique(getNames(iter.next()));
        rule.setAggregateFieldName(stripBrackets(iter.next()));
        rule.setAggregatorFunctionType(AggregatorFunctionType.valueOf(stripBrackets(iter.next()).toUpperCase()));
        rule.setLimitOperatorType(LimitOperatorType.fromString(stripBrackets(iter.next())));
        rule.setLimit(new BigDecimal(stripBrackets(iter.next())));
        rule.setWindowMinutes(Integer.parseInt(stripBrackets(iter.next())));

        return rule;
    }

    private static String stripBrackets(String expression) {
        return expression.replaceAll("[()]", "");
    }

    private static List<String> getNames(String expression) {
        String keyNamesString = expression.replaceAll("[()]", "");
        if (!keyNamesString.isEmpty()) {
            String[] tokens = keyNamesString.split("&", -1);
            return Arrays.asList(tokens);
        } else {
            return new ArrayList<>();
        }
    }
}
