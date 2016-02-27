package solver.csp.cspRowCol.heuristics.variable;

import solver.csp.Variable;
import solver.csp.cspRowCol.RowColVariable;
import solver.csp.heuristics.variable.VariableHeuristic;

import java.util.List;

/**
 * Created by Zohar on 27/02/2016.
 */
public class RowColLengthAndMaxSumHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<Variable> unassigned) {
        VariableHeuristic rowColLengthHeuristic = new RowColLengthHeuristic();
        VariableHeuristic rowColMaxSumHeuristic = new RowColMaxSumVariableHeuristic();
        RowColVariable var1 = (RowColVariable)rowColLengthHeuristic.select(unassigned);
        RowColVariable var2 = (RowColVariable)rowColMaxSumHeuristic.select(unassigned);
        return var1.getMaxBlock() > var2.getMaxBlock() ? var1 : var2;
    }
}
