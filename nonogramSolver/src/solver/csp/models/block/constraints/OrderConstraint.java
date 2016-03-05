package solver.csp.models.block.constraints;

import solver.csp.Variable;
import solver.csp.models.block.BlockVariable;
import solver.csp.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public class OrderConstraint extends Constraint {
    BlockVariable firstVar;
    BlockVariable secondVar;

    public OrderConstraint(BlockVariable firstVar, BlockVariable secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    @Override
    public boolean checkConstraintFails() {
        return !(((Integer) firstVar.getValue()) == null || ((Integer) secondVar.getValue()) == null) &&
                !(((Integer) firstVar.getValue()) + firstVar.getLength() < ((Integer) secondVar.getValue()));
    }

    public List<Variable> getAffectedVariables(){
        List<Variable> result = new ArrayList<Variable>(2);
        result.add(firstVar);
        result.add(secondVar);
        return result;
    }
}