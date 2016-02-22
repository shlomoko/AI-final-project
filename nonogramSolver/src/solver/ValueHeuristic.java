package solver;

import solver.Variable;
import solver.cspBlock.BlockVariable;

import java.util.List;

/**
 * Created by tmrlvi on 24/01/2016.
 */
public interface ValueHeuristic {
    List<Object> order(Variable to_assign);
}
