package solver.csp.cspBlock;

import solver.csp.Constraint;
import solver.csp.Variable;

import java.util.*;


/**
 * Created by Zohar on 24/01/2016.
 */
public class BlockVariable extends Variable{
    private Integer start;
    private int length;
    private boolean isRow;
    private int index;
    private Set<Integer> legalValues; // The possible indexes of start.
    private List<Constraint> constraints;
    private List<Integer> rowSum;

    public BlockVariable(int length, boolean isRow, int index, int maxIndex){
        this.start = null;
        this.length = length;
        this.isRow = isRow;
        this.index = index;
        constraints = new ArrayList<Constraint>();
        rowSum = new ArrayList<Integer>();
        legalValues = new TreeSet<Integer>();
        for (int i=0; i<maxIndex; i++){
            legalValues.add(i);
        }
    }

    public void addConstraint(Constraint constraint){
        constraints.add(constraint);
    }
    public void addRowSum(List<Integer> rowSum) {this.rowSum = rowSum;}

    /**
     * Returns the value of the array in the domain.
     * In our case, this is just a number. In fact - just the start location
     * of the block
     * @return the value of the variable - the start location.
     */
    public Object getValue(){
        return start;
    }

    public void setValue(Object startValue) {
        this.start = (Integer)startValue;
    }

    public Set<Object> getLegalValues(){
        // If assigned - the only legal value is the assigned one
        if (start != null){
            Set<Object> values = new HashSet<Object>();
            values.add(start);
            return values;
        }
        return new HashSet<Object>(legalValues);
    }

    public List<Constraint> getConstraints(){
        return constraints;
    };

    public void removeLegalValue(Object value){
        legalValues.remove((Integer) value);
    }

    public void addLegalValue(Object value){
        legalValues.add((Integer) value);
    }

    public int getLength(){
        return length;
    }

    public int getIndex(){
        return index;
    }

    public List<Integer> getRowSum() {return rowSum;}

    public boolean isRow(){
        return isRow;
    }

}
