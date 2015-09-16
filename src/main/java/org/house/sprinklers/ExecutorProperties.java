package org.house.sprinklers;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "executors")
@Data
public class ExecutorProperties {
    private int nThreads;
}
