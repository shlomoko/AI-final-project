package solver.csp.handlers;

import solver.csp.Constraint;
import solver.csp.ConstraintHandler;
import solver.csp.Manager;
import solver.csp.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class ConstraintChecker implements ConstraintHandler {
    private Manager manager;
    private Map<Variable, List<Object>> trueValue;

    public ConstraintChecker(){
        trueValue = new HashMap<Variable, List<Object>>();
    }

    @Override
    public void setManager(Manager manager) {
        this.manager = manager;
    }

    @Override
    public boolean initialize(List<Variable> unassigned) {
        // All constraints are not violated
        return true;
    }

    @Override
    public Map<Variable, List<Object>> checkConstraints(Variable assigned) {
        for (Constraint constraint : assigned.getConstraints()){
            if (constraint.isViolated())
                return null;
        }
        return trueValue;
    }

    @Override
    public void restoreValue(Map<Variable, List<Object>> removedValues) {}
}
