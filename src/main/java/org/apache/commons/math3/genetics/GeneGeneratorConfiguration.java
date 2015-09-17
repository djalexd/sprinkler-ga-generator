package org.apache.commons.math3.genetics;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.validation.ValidationException;

@Configuration
@Data
public class GeneGeneratorConfiguration implements InitializingBean {
    private double probabilityRemoveGene;
    private int minGenesToRemove;
    private int maxGenesToRemove;

    private double probabilityInsertGenes;
    private int minGenesToInsert;
    private int maxGenesToInsert;

    private double probabilityChangeGenes;
    private int minGenesToChange;
    private int maxGenesToChange;

    private double probabilityChangePosX;
    private double probabilityChangePosY;
    private double probabilityChangeRadius;
    private double probabilityChangeAngleStart;
    private double probabilityChangeAngleEnd;

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO This is JSR303 validation for the entire object,
        // and cannot stay here.
        double total = probabilityInsertGenes + probabilityRemoveGene + probabilityChangeGenes;
        if (total < 0.99 && total > 1.01) {
            throw new ValidationException("Total probability of insertGene/removeGene/newGene is not 1.0");
        }
    }
}
