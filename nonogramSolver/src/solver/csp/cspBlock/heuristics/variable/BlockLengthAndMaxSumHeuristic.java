package solver.csp.cspBlock.heuristics.variable;

import solver.csp.Variable;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.cspBlock.BlockVariable;

import java.util.List;

/**
 * Created by Zohar on 10/02/2016.
 */
public class BlockLengthAndMaxSumHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<Variable> unassigned) {
        VariableHeuristic blockLengthHeuristic = new BlockLengthHeuristic();
        VariableHeuristic MaxSumHeuristic = new MaxSumVariableHeuristic();
        BlockVariable var1 = (BlockVariable)blockLengthHeuristic.select(unassigned);
        BlockVariable var2 = (BlockVariable)MaxSumHeuristic.select(unassigned);
        return var1.getLength() > var2.getLength() ? var1 : var2;
    }
}
