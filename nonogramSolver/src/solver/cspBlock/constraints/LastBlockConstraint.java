package solver.cspBlock.constraints;

import solver.Variable;
import solver.cspBlock.BlockVariable;
import solver.Constraint;

import java.util.ArrayList;
import java.util.List;

public class LastBlockConstraint implements Constraint {
    BlockVariable block;
    int lastIndex;

    public LastBlockConstraint(BlockVariable block, int lastIndex){
        this.block = block;
        this.lastIndex = lastIndex;
    }

    @Override
    public boolean isViolated() {
        // Constaint is violated if block isn't null and extends beyond row/column
        return !(block.getValue() == null) && (((Integer)block.getValue()) + block.getLength() > lastIndex);
    }

    @Override
    public List<Variable> getAffectedVariables() {
        List<Variable> result = new ArrayList<Variable>();
        result.add(block);
        return result;
    }
}
