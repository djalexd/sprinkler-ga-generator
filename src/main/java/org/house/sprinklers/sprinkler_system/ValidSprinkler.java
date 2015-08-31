package org.house.sprinklers.sprinkler_system;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSprinklerValidator.class)
public @interface ValidSprinkler {

    String message() default "Sprinkler not valid";

    // Required by validation runtime
    Class<?>[] groups() default {};

    // Required by validation runtime
    Class<? extends Payload>[] payload() default {};

    // Custom fields

    double minRange() default 0.0;

    double maxRange() default 10.0;
}
