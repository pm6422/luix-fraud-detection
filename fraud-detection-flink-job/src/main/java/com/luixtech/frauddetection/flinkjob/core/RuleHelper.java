package com.luixtech.frauddetection.flinkjob.core;

import com.luixtech.frauddetection.common.rule.ControlType;
import com.luixtech.frauddetection.flinkjob.core.accumulator.AverageAccumulator;
import com.luixtech.frauddetection.flinkjob.core.accumulator.BigDecimalCounter;
import com.luixtech.frauddetection.flinkjob.core.accumulator.BigDecimalMaximum;
import com.luixtech.frauddetection.flinkjob.core.accumulator.BigDecimalMinimum;
import com.luixtech.frauddetection.common.dto.Rule;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.accumulators.SimpleAccumulator;
import org.apache.flink.api.common.state.BroadcastState;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/* Collection of helper methods for Rules. */
@Slf4j
public class RuleHelper {
    public static void handleRule(BroadcastState<Integer, Rule> broadcastState, Rule rule) throws Exception {
        switch (rule.getRuleState()) {
            case ACTIVE:
            case PAUSE:
                // merge rule
                broadcastState.put(rule.getRuleId(), rule);
                break;
            case DELETE:
                broadcastState.remove(rule.getRuleId());
                break;
            case CONTROL:
                handleControlCommand(broadcastState, rule.getControlType());
                break;
        }
    }

    private static void handleControlCommand(BroadcastState<Integer, Rule> rulesState, ControlType controlType) throws Exception {
        if (Objects.requireNonNull(controlType) == ControlType.DELETE_ALL_RULES) {
            Iterator<Map.Entry<Integer, Rule>> entriesIterator = rulesState.iterator();
            while (entriesIterator.hasNext()) {
                Map.Entry<Integer, Rule> ruleEntry = entriesIterator.next();
                rulesState.remove(ruleEntry.getKey());
                log.info("Removed {}", ruleEntry.getValue());
            }
        }
    }

    /* Picks and returns a new accumulator, based on the Rule's aggregator function type. */
    public static SimpleAccumulator<BigDecimal> getAggregator(Rule rule) {
        switch (rule.getAggregatorFunctionType()) {
            case SUM:
                return new BigDecimalCounter();
            case AVG:
                return new AverageAccumulator();
            case MAX:
                return new BigDecimalMaximum();
            case MIN:
                return new BigDecimalMinimum();
            default:
                throw new RuntimeException(
                        "Unsupported aggregation function type: " + rule.getAggregatorFunctionType());
        }
    }
}
