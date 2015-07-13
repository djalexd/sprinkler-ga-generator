package org.house.sprinklers.sprinkler_system;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.population.SprinklerValidator;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

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

            final List<Sprinkler> sprinklers = Lists.newArrayListWithExpectedSize(numSprinklers);
            log.info("Found {} sprinklers in genome of length {}", numSprinklers, genome.genes.length);

            for (int i = 0; i < numSprinklers; i++) {
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
                    Sprinkler sprinkler = new Sprinkler(new Point2D.Double(x, y), r, sa, ea, null);

                    // Perform validation of all data.
                    if (sprinklerValidator.isPresent()) {
                        try {
                            sprinklerValidator.get().validate(sprinkler);
                            sprinklers.add(sprinkler);
                        } catch (InvalidSprinklerException e) {
                            throw new InvalidGeneException(e);
                        }
                    } else {
                        sprinklers.add(sprinkler);
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
            }

            return new SprinklerSystem(sprinklers);
        }
    }

    @Slf4j
    public static class GenomeStore implements Store<SprinklerSystem, SprinklerSystemGenome> {
        @Override
        public SprinklerSystemGenome save(SprinklerSystem sprinklerSystem) throws Exception {
            final List<Sprinkler> sprinklers = sprinklerSystem.getSprinklers();

            int numGenes = 40 * sprinklers.size();

            log.info("Store sprinkler system in genome generated {} genes", numGenes);

            ByteBuffer wrap = ByteBuffer.allocate(numGenes);
            for (int i = 0 ; i < sprinklers.size(); i++) {
                int offset = 40 * i;

                wrap.putDouble(offset, sprinklers.get(i).getPosition().getX());
                wrap.putDouble(offset + 8, sprinklers.get(i).getPosition().getY());
                wrap.putDouble(offset + 16, sprinklers.get(i).getRange());
                wrap.putDouble(offset + 24, sprinklers.get(i).getStartAngle());
                wrap.putDouble(offset + 32, sprinklers.get(i).getEndAngle());
            }

            return new SprinklerSystemGenome(wrap.array());
        }

    }
}
