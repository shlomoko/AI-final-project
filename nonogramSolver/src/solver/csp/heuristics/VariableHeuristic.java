package solver.csp.heuristics;

import solver.csp.Variable;

import java.util.List;

/**
 * Created by tmrlvi on 24/01/2016.
 */
public interface VariableHeuristic {
    public Variable select(List<Variable> unassigned);
}
