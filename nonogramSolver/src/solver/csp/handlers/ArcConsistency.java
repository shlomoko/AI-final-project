package solver.csp.handlers;

import solver.csp.Constraint;
import solver.csp.ConstraintHandler;
import solver.csp.Manager;
import solver.csp.Variable;

import java.util.*;

/**
 * Created by tmrlvi on 23/02/2016.
 */
public class ArcConsistency implements ConstraintHandler {
    private Manager manager = null;

    private class Arc{
        public Variable source;
        public Constraint constaint;

        public Arc(Variable source, Constraint constaint){
            this.source = source;
            this.constaint = constaint;
        }
    }

    public void setManager(Manager manager){
        this.manager = manager;
    }

    private Map<Variable, List<Object>> generalizedArcConsistency(Queue<Arc> arcs){
        Map<Variable, List<Object>> inconsistentValues = new HashMap<Variable, List<Object>>();

        while (!arcs.isEmpty()){
            if (manager != null && manager.isStopped()){
                // Requested termination
                //TODO: should emit an exception?
                return null;
            }
            Arc arc = arcs.remove();

            // Go over different instantiation and check if the relevant constraint is violated
            List<Object> values = _removeInconsistentValue(arc.source, arc.constaint);

            // arc.source changed, so we need to add all others:
            if (values.size() > 0) {
                if (inconsistentValues.containsKey(arc.source)){
                    inconsistentValues.get(arc.source).addAll(values);
                } else {
                    inconsistentValues.put(arc.source, values);
                }

                // If an arc remained with no domain, we've lost.
                if (arc.source.getLegalValues().size() == 0) {
                    // Return all nodes lost so far
                    restoreValue(inconsistentValues);
                    return null;
                }
                // We removed stuff - we need to take care of the neighbors
                for (Constraint cons : arc.source.getConstraints())
                    for (Variable first : cons.getAffectedVariables())
                        if (first != arc.source)
                            arcs.add(new Arc(first, cons));
            }
        }
        return inconsistentValues;
    }

    // For every value in the domain, checks if there is any assignment s.t. the constraint would not be violated.
    private List<Object> _removeInconsistentValue(Variable source, Constraint cons){
        Queue<Variable> others = new LinkedList<Variable>(cons.getAffectedVariables());
        others.remove(source);
        Object previousX = source.getValue();
        List<Object> removed = new LinkedList<Object>();
        List<Object> legalValues = new LinkedList<Object>(source.getLegalValues());
        for (Object x : legalValues){
            // Note that there is not harm in resetting the variables - legal values is the only value if only
            // such value possible.
            source.setValue(x);
            if (!_couldSatisfyConstraint(cons, others)){
                removed.add(x);
                source.removeLegalValue(x);
            }
        }
        source.setValue(previousX);
        return removed;
    }

    private boolean _couldSatisfyConstraint(Constraint cons, Queue<Variable> remainingRowColVariables){
        if (remainingRowColVariables.size() == 0)
            return !cons.isViolated(); // The constraint is not violated!
        //Copy, so we don't ruin;
        remainingRowColVariables = new LinkedList<Variable>(remainingRowColVariables);
        Variable next_var = remainingRowColVariables.remove();
        Object originalValue = next_var.getValue();
        for (Object value : next_var.getLegalValues()){
            next_var.setValue(value);
            if (_couldSatisfyConstraint(cons, remainingRowColVariables)) {
                // Return to the original value
                next_var.setValue(originalValue);
                return true;
            }
        }
        // Return to the original value
        next_var.setValue(originalValue);
        return false;
    }

    @Override
    public boolean initialize(List<Variable> unassigned) {
        Queue<Arc> arcs = new LinkedList<Arc>();
        for (Variable first : unassigned){
            for (Constraint cons : first.getConstraints())
                arcs.add(new Arc(first, cons));
        }
        return generalizedArcConsistency(arcs) != null;
    }

    @Override
    public Map<Variable, List<Object>> checkConstraints(Variable var) {
        // Returns a map of removed values, if detected inconsistency, returns null.

        Queue<Arc> arcs = new LinkedList<Arc>();
        for (Constraint constr : var.getConstraints()){
            arcs.add(new Arc(var, constr));
        }

        return generalizedArcConsistency(arcs);
    }

    @Override
    public void restoreValue(Map<Variable, List<Object>> removedValues) {
        if (removedValues == null) return;
            for (Map.Entry<Variable, List<Object>> neighbor : removedValues.entrySet()){
                for (Object value : neighbor.getValue()){
                    neighbor.getKey().addLegalValue(value);
                }
            }
    }
}
