package com.luixtech.frauddetection.flinkjob.config;

import static org.junit.Assert.assertEquals;

import com.luixtech.frauddetection.flinkjob.input.ParamHolder;
import com.luixtech.frauddetection.flinkjob.input.Parameters;
import org.junit.Test;

public class ConfigTest {

  @Test
  public void testParameters() {
    String[] args = new String[] {"--kafka-host", "host-from-args"};
    Parameters parameters = Parameters.fromArgs(args);
    ParamHolder paramHolder = ParamHolder.fromParameters(parameters);

    final String kafkaHost = paramHolder.getValue(Parameters.KAFKA_HOST);
    assertEquals("Wrong config parameter retrieved", "host-from-args", kafkaHost);
  }

  @Test
  public void testParameterWithDefaults() {
    String[] args = new String[] {};
    Parameters parameters = Parameters.fromArgs(args);
    ParamHolder paramHolder = ParamHolder.fromParameters(parameters);

    final Integer kafkaPort = paramHolder.getValue(Parameters.KAFKA_PORT);
    assertEquals("Wrong config parameter retrieved", Integer.valueOf(9092), kafkaPort);
  }
}
