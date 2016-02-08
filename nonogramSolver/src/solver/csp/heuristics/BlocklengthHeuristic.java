package solver.csp.heuristics;

import solver.csp.Variable;

import java.util.List;

/**
 * Created by tmrlvi on 08/02/2016.
 */
public class BlockLengthHeuristic implements VariableHeuristic {

    @Override
    public Variable select(List<Variable> unassigned) {
        int maxLength = -1;
        Variable maxBlock = null;
        for (Variable block : unassigned){
            if (block.getLength() > maxLength){
                maxLength = block.getLength();
                maxBlock = block;
            }
        }
        return maxBlock;
    }
}
