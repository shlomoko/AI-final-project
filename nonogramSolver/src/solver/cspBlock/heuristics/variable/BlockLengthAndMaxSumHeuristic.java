package solver.cspBlock.heuristics.variable;

import solver.cspBlock.Variable;

import java.util.List;

/**
 * Created by Zohar on 10/02/2016.
 */
public class BlockLengthAndMaxSumHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<Variable> unassigned) {
        VariableHeuristic blockLengthHeuristic = new BlockLengthHeuristic();
        VariableHeuristic MaxSumHeuristic = new MaxSumVariableHeuristic();
        Variable var1 = blockLengthHeuristic.select(unassigned);
        Variable var2 = MaxSumHeuristic.select(unassigned);
        return var1.getLength() > var2.getLength() ? var1 : var2;
    }
}
