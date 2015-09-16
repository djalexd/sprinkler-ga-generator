package org.house.sprinklers;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@ConfigurationProperties(prefix = "terrain")
@Data
public class TerrainProperties {
    private String data;

    public Resource terrainAsResource() {
        return new ClassPathResource(data);
    }
}
