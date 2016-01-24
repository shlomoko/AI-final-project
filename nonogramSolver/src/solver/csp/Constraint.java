package solver.csp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public abstract class Constraint {
    protected Variable firstVar;
    protected Variable secondVar;

    public Constraint(Variable firstVar, Variable secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    public abstract boolean isViolated();

    public List<Variable> getAffectedVariables(){
        List<Variable> result = new ArrayList<Variable>(2);
        result.add(firstVar);
        result.add(secondVar);
        return result;
    }
}
