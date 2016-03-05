package solver.csp.models.cell;

import solver.csp.Constraint;
import solver.csp.Variable;

import java.util.*;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class CellVariable extends Variable {
    Integer row;
    Integer column;
    Boolean value;
    List<Constraint> constraints;
    Set<Object> legalValues;

    public CellVariable(Integer row, Integer column){
        this.row = row;
        this.column = column;
        constraints = new ArrayList<Constraint>();
        legalValues = new HashSet<Object>();
        legalValues.add(true);
        legalValues.add(false);
    }

    @Override
    public void setValue(Object value) {
        this.value = (Boolean) value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Set<Object> getLegalValues() {
        return legalValues;
    }

    @Override
    public void addLegalValue(Object value) {
        legalValues.add(value);
    }

    @Override
    public void removeLegalValue(Object value) {
        legalValues.remove(value);
    }

    @Override
    public List<Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    public Integer getRow() {
        return row;
    }

    public Integer getColumn() {
        return column;
    }
}
