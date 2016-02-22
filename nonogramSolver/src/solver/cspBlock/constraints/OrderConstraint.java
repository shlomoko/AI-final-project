package solver.cspBlock.constraints;

import solver.Variable;
import solver.cspBlock.BlockVariable;
import solver.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public class OrderConstraint implements Constraint {
    BlockVariable firstVar;
    BlockVariable secondVar;

    public OrderConstraint(BlockVariable firstVar, BlockVariable secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    @Override
    public boolean isViolated() {
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