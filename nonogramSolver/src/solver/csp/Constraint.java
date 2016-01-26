package solver.csp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public interface Constraint {

    public abstract boolean isViolated();
    public List<Variable> getAffectedVariables();
}
