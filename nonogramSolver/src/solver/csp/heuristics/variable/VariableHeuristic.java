package solver.csp.heuristics.variable;

import solver.csp.Variable;

import java.util.List;

/**
 * Created by tmrlvi on 24/01/2016.
 */
public interface VariableHeuristic {
    public Variable select(List<? extends Variable> unassigned);
}
