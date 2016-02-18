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
    private List<Integer> blocks;
    private int length;
    private boolean isRow;
    private int index;
    private Set<List<Integer>> legalValues; // The possible indexes of start.
    private List<Constraint> constraints;

    public Variable(List<Integer> blocks, boolean isRow, int index, int length){
        this.blocks = blocks;
        this.isRow = isRow;
        this.index = index;
        this.length = length;
        constraints = new ArrayList<Constraint>();
        legalValues = new TreeSet<List<Integer>>();
    }

    public void addConstraint(Constraint constraint){
        constraints.add(constraint);
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
