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

    /**
     * Returns the value of the array in the domain.
     * In our case, this is just a number. In fact - just the start location
     * of the block
     * @return the value of the variable - the start location.
     */
    public int getStartValue(){
        return start;
    }
}
