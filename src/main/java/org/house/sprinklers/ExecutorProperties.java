package org.house.sprinklers;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ConfigurationProperties(prefix = "executors")
@Data
public class ExecutorProperties {
    private int nThreads;

    @Bean
    ExecutorService executor() {
        return Executors.newFixedThreadPool(getNThreads());
    }
}
