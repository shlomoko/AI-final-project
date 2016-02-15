package solver.cspBlock.constraints;

import solver.cspBlock.Variable;
import solver.cspBlock.constraints.Constraint;

import java.util.ArrayList;
import java.util.List;

public class LastBlockConstraint implements Constraint {
    Variable block;
    int lastIndex;

    public LastBlockConstraint(Variable block, int lastIndex){
        this.block = block;
        this.lastIndex = lastIndex;
    }

    @Override
    public boolean isViolated() {
        // Constaint is violated if block isn't null and extends beyond row/column
        return !(block.getStartValue() == null) && (block.getStartValue() + block.getLength() > lastIndex);
    }

    @Override
    public List<Variable> getAffectedVariables() {
        List<Variable> result = new ArrayList<Variable>();
        result.add(block);
        return result;
    }
}
