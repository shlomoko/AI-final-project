package solver.csp.models.block.heuristics.variable;

import solver.annotations.LimitTo;
import solver.csp.Variable;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.block.BlockManager;
import solver.csp.models.block.BlockVariable;

import java.util.List;

/**
 * Created by tmrlvi on 08/02/2016.
 */
@LimitTo("BlockManager")
public class BlockLengthHeuristic implements VariableHeuristic {

    @Override
    public Variable select(List<? extends Variable> unassigned) {
        int maxLength = -1;
        Variable maxBlock = null;
        for (Variable basicBlock : unassigned){
            BlockVariable block = (BlockVariable) basicBlock;
            if (block.getLength() > maxLength){
                maxLength = block.getLength();
                maxBlock = block;
            }
        }
        return maxBlock;
    }

}
