package solver.csp.models.rowcol.heuristics.variable;

import solver.annotations.LimitTo;
import solver.csp.Variable;
import solver.csp.models.rowcol.RowColVariable;
import solver.csp.heuristics.variable.VariableHeuristic;

import java.util.List;

/**
 * Created by Zohar on 25/02/2016.
 */
@LimitTo("RowColManager")
public class RowColLengthHeuristic implements VariableHeuristic{
    @Override
    public Variable select(List<? extends Variable> unassigned) {
        int maxLength = 0;
        RowColVariable maxLenVar = null;
        for (Variable basicVar : unassigned){
            RowColVariable var = (RowColVariable)basicVar;
                if(var.getMaxBlock() >= maxLength){
                    maxLength = var.getMaxBlock();
                    maxLenVar = var;
                }
        }
        return maxLenVar;
    }
}
