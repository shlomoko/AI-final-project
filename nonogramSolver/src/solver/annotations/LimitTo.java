package solver.annotations;

import solver.csp.Manager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tmrlvi on 04/03/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitTo {
    String value();
}
