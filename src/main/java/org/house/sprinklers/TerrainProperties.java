package org.house.sprinklers;

import lombok.Data;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "terrain")
@Data
public class TerrainProperties {
    private String data;

    @Bean
    Terrain terrain() {
        try {
            return new Terrain.TerrainLoader().load(new ClassPathResource(data).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Terrain", e);
        }
    }
}
