package solver.csp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public class OrderConstraint implements Constraint {
    Variable firstVar;
    Variable secondVar;

    public OrderConstraint(Variable firstVar, Variable secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    @Override
    public boolean isViolated() {
        return !(firstVar.getStartValue() == null || secondVar.getStartValue() == null) &&
                !(firstVar.getStartValue() + firstVar.getLength() < secondVar.getStartValue());
    }

    public List<Variable> getAffectedVariables(){
        List<Variable> result = new ArrayList<Variable>(2);
        result.add(firstVar);
        result.add(secondVar);
        return result;
    }
}