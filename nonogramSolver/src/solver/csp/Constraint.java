package solver.csp;

import solver.measure.Counters;

import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public abstract class Constraint {
    private static Counters counters = Counters.getInstance();

    public boolean isViolated(){
        counters.addCount("Constraints", 1L);
        counters.addCount("Constraints times amount of variables", (long) getAffectedVariables().size());
        return checkConstraintFails();
    }

    public abstract boolean checkConstraintFails();
    public abstract List<Variable> getAffectedVariables();
}
