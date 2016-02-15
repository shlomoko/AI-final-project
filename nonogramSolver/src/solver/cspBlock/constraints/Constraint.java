package solver.cspBlock.constraints;

import solver.cspBlock.Variable;

import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public interface Constraint {

    public abstract boolean isViolated();
    public List<Variable> getAffectedVariables();
}
