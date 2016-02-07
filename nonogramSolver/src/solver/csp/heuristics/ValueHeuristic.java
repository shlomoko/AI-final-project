package solver.csp.heuristics;

import solver.csp.Variable;

import java.util.List;

/**
 * Created by tmrlvi on 24/01/2016.
 */
public interface ValueHeuristic {
    List<Integer> order(Variable to_assign);
}
