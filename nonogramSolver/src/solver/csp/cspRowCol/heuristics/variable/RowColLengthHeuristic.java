package solver.csp.cspRowCol.heuristics.variable;

import solver.csp.Variable;
import solver.csp.cspRowCol.RowColVariable;
import solver.csp.heuristics.variable.VariableHeuristic;

import java.util.List;

/**
 * Created by Zohar on 25/02/2016.
 */
public class RowColLengthHeuristic implements VariableHeuristic{
    @Override
    public Variable select(List<Variable> unassigned) {
        int maxLength = 1;
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
