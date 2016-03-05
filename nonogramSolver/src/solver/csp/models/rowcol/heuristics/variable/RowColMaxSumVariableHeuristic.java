package solver.csp.models.rowcol.heuristics.variable;

import solver.annotations.Default;
import solver.annotations.LimitTo;
import solver.csp.Variable;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.rowcol.RowColVariable;

import java.util.Collections;
import java.util.List;

/**
 * Created by Zohar on 26/02/2016.
 */
@Default
@LimitTo("RowColManager")
public class RowColMaxSumVariableHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<? extends Variable> unassigned) {
        int maxSum = 0;
        RowColVariable maxSumVar = null;
        for(Variable basicVar : unassigned){
            RowColVariable var = (RowColVariable)basicVar;
            if(var.getBlocksSum() >=maxSum){
                maxSum = var.getBlocksSum();
                maxSumVar = var;
            }
        }
        return  maxSumVar;
    }
}
