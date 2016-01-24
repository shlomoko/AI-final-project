package solver.csp;

import java.util.List;
import java.util.Set;
;

/**
 * Created by Zohar on 24/01/2016.
 */
public class Variable {
    private int start;
    private int length;
    private boolean isRow;
    private int index;
    private Set<Integer> legalValues; // The possible indexes of start.
    private List<Constraint> constraints;
}
