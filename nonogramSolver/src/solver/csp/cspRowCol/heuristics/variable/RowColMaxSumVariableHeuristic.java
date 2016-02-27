package solver.csp.cspRowCol.heuristics.variable;

import solver.csp.Variable;
import solver.csp.cspRowCol.RowColVariable;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;

import java.util.Collections;
import java.util.List;

/**
 * Created by Zohar on 26/02/2016.
 */
public class RowColMaxSumVariableHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<Variable> unassigned) {
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
