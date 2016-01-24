package solver.csp.heuristics;

import java.util.List;
import java.util.Set;

/**
 * Created by tmrlvi on 24/01/2016.
 */
public interface ConstraintHeuristic {
    List<Integer> order(Set<Integer> possibleValues);
}
