package org.house.sprinklers.genetics;

import org.apache.commons.math3.genetics.Population;

public interface PopulationListener {
    /**
     *
     * @param durationInMillis TODO This is a cross-cutting concern and should not be placed here
     */
    void onPopulation(Population current, int generation, long durationInMillis);
}
