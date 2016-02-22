package solver.cspRowCol;

import solver.Constraint;
import solver.Variable;

import java.util.*;


/**
 * Created by Zohar on 24/01/2016.
 */
public class RowColVariable implements Variable {
    public class Edges{
        public int start;
        public int end;

        public Edges(int start, int end){
            this.start = start;
            this.end = end;
        }
    }

    private List<Integer> blocks;
    private int length;
    private boolean isRow;
    private int index;
    private List<Edges> value;
    private Set<List<Edges>> legalValues; // The possible indexes of start.
    private List<Constraint> constraints;

    public RowColVariable(List<Integer> blocks, boolean isRow, int index, int length){
        this.blocks = blocks;
        this.isRow = isRow;
        this.value = null;
        this.index = index;
        this.length = length;
        constraints = new ArrayList<Constraint>();
        legalValues = new HashSet<List<Edges>>();
        populateLegalValues(0, new ArrayList<Edges>());
    }
    private void populateLegalValues(int index, List<Edges> list){
        int location;
        if(index == 0){
            location = 0;
        }else if(index == blocks.size()){
            legalValues.add(list);
            return;
        }else {
            location = list.get(list.size() - 1).end + 1;
        }
        for (int i=location; i<=length-blocks.get(index); i++){
            List<Edges> temp = new ArrayList<Edges>(list);
            temp.add(new Edges(i, i+blocks.get(index)));
            populateLegalValues(index+1, temp);
        }
    }

    public void addConstraint(Constraint constraint){
        constraints.add(constraint);
    }

    public Object getValue(){
        return value;
    }

    public void setValue(Object value){
        //add instance of
        this.value = (List<Edges>) value;
    }

    public Set<Object> getLegalValues(){
        // If assigned - the only legal value is the assigned one
        if (value != null){
            Set<Object> values = new HashSet<Object>();
            values.add(value);
            return values;
        }
        return new HashSet<Object>(legalValues);
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

    public void removeLegalValue(Object value){
        legalValues.remove(value);
    }

    public void addLegalValue(Object value){
        legalValues.add((List<Edges>) value);
    }

    public int getLength(){
        return length;
    }

    public int getIndex(){
        return index;
    }

    public boolean isRow(){
        return isRow;
    }

    public boolean isLegalValue(Object value){
        List<Edges> oldValue = this.value;
        setValue(value);
        for (Constraint constr : getConstraints()){
            if (constr.isViolated()){
                setValue(oldValue);
                return false;
            }
        }
        setValue(oldValue);
        return true;
    }

}
