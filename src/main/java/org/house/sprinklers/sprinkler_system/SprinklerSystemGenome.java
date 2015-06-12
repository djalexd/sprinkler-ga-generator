package org.house.sprinklers.sprinkler_system;

import com.google.common.base.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.population.SprinklerValidator;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Data
public class SprinklerSystemGenome {

    private final byte[] genes;

    @Slf4j
    public static class GenomeLoader implements Loader<SprinklerSystemGenome, SprinklerSystem> {

        private Optional<SprinklerValidator> sprinklerValidator;

        public GenomeLoader(Optional<SprinklerValidator> sprinklerValidator) {
            this.sprinklerValidator = sprinklerValidator;
        }

        @Override
        public SprinklerSystem load(SprinklerSystemGenome genome) throws Exception {
            ByteBuffer wrap = ByteBuffer.wrap(genome.genes);

            int numSprinklers = genome.genes.length / 40;
            int invalidGenes = 0;

            final Sprinkler[] sprinklers = new Sprinkler[numSprinklers];
            log.info("Found {} sprinklers in genome of length {}", sprinklers.length, genome.genes.length);

            for (int i = 0; i < sprinklers.length; i++) {
                int offset = 40 * i;

                // Provide corrections to the model. If a specific value cannot be read,
                // reduce the number of sprinklers (malformed gene at index [i], but further
                // genes could still be valid).

                try {
                    double x = wrap.getDouble(offset),
                            y = wrap.getDouble(offset + 8),
                            r = wrap.getDouble(offset + 16),
                            sa = wrap.getDouble(offset + 24),
                            ea = wrap.getDouble(offset + 32);
                    sprinklers[i] = new Sprinkler(new Point2D.Double(x, y), r, sa, ea, null);

                    // Perform validation of all data.
                    if (sprinklerValidator.isPresent()) {
                        try {
                            sprinklerValidator.get().validate(sprinklers[i]);
                        } catch (InvalidSprinklerException e) {
                            throw new InvalidGeneException(e);
                        }
                    }

                } catch (InvalidGeneException e) {

                    // Log the exception and continue
                    log.warn("Invalid gene found at sprinkler #{}", i, e);
                    invalidGenes++;

                    // Fix the next gene index
                    i--;
                }
            }

            if (invalidGenes > 0) {
                log.info("Found {} invalid genes, the overall genome was reduced from {} to {} genes", invalidGenes,
                        genome.genes.length, genome.genes.length - invalidGenes * 40);
                return new SprinklerSystem(Arrays.copyOfRange(sprinklers, 0, sprinklers.length - invalidGenes));
            } else {
                return new SprinklerSystem(sprinklers);
            }
        }
    }

    @Slf4j
    public static class GenomeStore implements Store<SprinklerSystem, SprinklerSystemGenome> {
        @Override
        public SprinklerSystemGenome save(SprinklerSystem sprinklerSystem) throws Exception {
            final Sprinkler[] sprinklers = sprinklerSystem.getSprinklers();

            int numGenes = 40 * sprinklers.length;

            log.info("Store sprinkler system in genome generated {} genes", numGenes);

            ByteBuffer wrap = ByteBuffer.allocate(numGenes);
            for (int i = 0 ; i < sprinklers.length; i++) {
                int offset = 40 * i;
                wrap.putDouble(offset, sprinklers[i].getPosition().getX());
                wrap.putDouble(offset + 8, sprinklers[i].getPosition().getY());
                wrap.putDouble(offset + 16, sprinklers[i].getRange());
                wrap.putDouble(offset + 24, sprinklers[i].getStartAngle());
                wrap.putDouble(offset + 32, sprinklers[i].getEndAngle());
            }

            return new SprinklerSystemGenome(wrap.array());
        }

    }
}
