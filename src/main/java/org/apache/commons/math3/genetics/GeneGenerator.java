package org.apache.commons.math3.genetics;

public interface GeneGenerator<T> {
    /**
     * Generates a new gene.
     */
    T generateRandomGene();
}
