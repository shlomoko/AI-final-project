package solver.cspRowCol.constraints;

import solver.Constraint;
import solver.Variable;
import solver.cspRowCol.RowColVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 22/02/2016.
 */
public class RowColConstraint implements Constraint {
    private RowColVariable row;
    private RowColVariable col;

    public RowColConstraint(RowColVariable row, RowColVariable col){
        this.row = row;
        this.col = col;
    }

    private boolean isMarked(int location, List<RowColVariable.Edges> list){
        for (RowColVariable.Edges edge : list){
            if (location < edge.end){
                return location >= edge.start;
            }
        }
        return false;
    }

    @Override
    public boolean isViolated() {
        List<RowColVariable.Edges> colValues = (List<RowColVariable.Edges>) col.getValue();
        List<RowColVariable.Edges> rowValues = (List<RowColVariable.Edges>) row.getValue();
        if (colValues != null && rowValues != null){
            boolean rowMarked = isMarked(col.getIndex(), rowValues);
            boolean colMarked = isMarked(row.getIndex(), colValues);
            return (rowMarked && !colMarked) || (!rowMarked && colMarked);
        }
        return false;
    }

    @Override
    public List<Variable> getAffectedVariables() {
        List<Variable> list = new ArrayList<Variable>();
        list.add(row);
        list.add(col);
        return list;
    }
}

