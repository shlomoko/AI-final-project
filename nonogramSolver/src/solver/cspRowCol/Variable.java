package solver.cspRowCol;

import solver.cspBlock.constraints.Constraint;

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
    private List<Integer> rowSum;

    public Variable(int length, boolean isRow, int index){
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
    public Integer getStartValue(){
        return start;
    }

    public void setStartValue(Integer startValue) {
        this.start = startValue;
    }

    public Set<Integer> getLegalValues(){
        // If assigned - the only legal value is the assigned one
        if (start != null){
            Set<Integer> values = new TreeSet<Integer>();
            values.add(start);
            return values;
        }
        return legalValues;
    }

    public List<Constraint> getConstraints(){
        return constraints;
    };

    public int getConstraintNumber(){
        int sum = 0;
        for (Constraint cons : constraints){
            sum = sum + cons.getAffectedVariables().size();
        }
        return sum;
    }

    public void removeLegalValue(Integer value){
        legalValues.remove(value);
    }

    public void addLegalValue(Integer value){
        legalValues.add(value);
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

    public boolean isLegalValue(Integer value){
        Integer oldValue = getStartValue();
        setStartValue(value);
        for (Constraint constr : getConstraints()){
            if (constr.isViolated()){
                setStartValue(oldValue);
                return false;
            }
        }
        setStartValue(oldValue);
        return true;
    }

}
