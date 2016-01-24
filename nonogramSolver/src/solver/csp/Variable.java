package solver.csp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Created by Zohar on 24/01/2016.
 */
public class Variable {
    private Integer start;
    private int length;
    private boolean isRow;
    private int index;
    private Set<Integer> legalValues; // The possible indexes of start.
    private List<Constraint> constraints;

    public Variable(int length, boolean isRow, int index, int maxIndex){
        this.start = null;
        this.length = length;
        this.isRow = isRow;
        this.index = index;
        constraints = new ArrayList<Constraint>();
        legalValues = new TreeSet<Integer>();
        for (int i=0; i<maxIndex; i++){
            legalValues.add(i);
        }
    }

    public void addConstraint(Constraint constraint){
        constraints.add(constraint);
    }

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
