package com.ververica.demo.backend.component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;

@Component
@AllArgsConstructor
@Slf4j
public class PrintAppInfoApplicationRunner implements ApplicationRunner {
    private final Environment env;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String appBanner =
                StreamUtils.copyToString(
                        new ClassPathResource("config/banner-app.txt").getInputStream(),
                        Charset.defaultCharset());
        log.info(
                appBanner,
                env.getProperty("spring.application.name"),
                "http",
                "127.0.0.1",
                env.getProperty("server.port"),
                StringUtils.defaultString(env.getProperty("server.servlet.context-path")),
                "http",
                "127.0.0.1",
                env.getProperty("server.port"),
                StringUtils.defaultString(env.getProperty("server.servlet.context-path")),
                org.springframework.util.StringUtils.arrayToCommaDelimitedString(env.getActiveProfiles()),
                env.getProperty("PID"),
                Charset.defaultCharset(),
                env.getProperty("logging.level.root"));
    }
}
