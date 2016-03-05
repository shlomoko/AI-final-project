package solver.csp.models.cell.heuristics;

import solver.annotations.LimitTo;
import solver.csp.Variable;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.cell.CellVariable;

import java.util.List;

/**
 * Created by tmrlvi on 24/02/2016.
 */
@LimitTo("CellManager")
public class EarlierHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<? extends Variable> unassigned) {
        CellVariable minVar = null;
        for (Variable var : unassigned){
            if (minVar == null || ((CellVariable) var).getRow() < minVar.getRow()
                || ( ((CellVariable) var).getRow() == minVar.getRow() &&
                     ((CellVariable) var).getColumn() == minVar.getColumn() )){
                minVar = (CellVariable) var;
            }
        }
        return minVar;
    }
}
