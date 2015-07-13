package org.house.sprinklers;

import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SprinklerRenderConfiguration {

    @Bean
    GameRenderer gameRenderer(Terrain terrain) {
        return new GameRenderer(terrain);
    }
}
