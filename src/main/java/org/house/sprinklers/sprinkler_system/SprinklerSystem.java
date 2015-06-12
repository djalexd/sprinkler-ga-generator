package org.house.sprinklers.sprinkler_system;

import lombok.Data;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

@Data
public class SprinklerSystem {

    private final Sprinkler[] sprinklers;

    public static class FileSystemLoader implements Loader<InputStream, SprinklerSystem> {
        @Override
        public SprinklerSystem load(InputStream in) throws Exception {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            int numSprinklers = Integer.parseInt(reader.readLine());
            Sprinkler[] sprinklers = new Sprinkler[numSprinklers];
            for (int i = 0; i < numSprinklers; i++) {

                String[] tokens = reader.readLine().split(" ");
                sprinklers[i] = new Sprinkler(
                        new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1])),
                        Double.parseDouble(tokens[2]),
                        Double.parseDouble(tokens[3]),
                        Double.parseDouble(tokens[4]),
                        null);

            }

            return new SprinklerSystem(sprinklers);
        }
    }

}