package solver.csp;

import solver.csp.heuristics.ValueHeuristic;
import solver.csp.heuristics.VariableHeuristic;

import java.util.*;

/**
 * Solves the CSP at hand using ArcConsistency and backtracking.
 */
public class CSPSolver {
    private class Arc{
        public Variable source;
        public Constraint constaint;

        public Arc(Variable source, Constraint constaint){
            this.source = source;
            this.constaint = constaint;
        }
    }

    List<Variable> variableList;
    List<Variable> unassigned;
    VariableHeuristic variableHeuristic; // Which variable to choose next
    ValueHeuristic valueHeuristic; // At what order we assign the values to the variable (startValues)
    CSPManager manager;

    //constructor
    public CSPSolver(List<Variable> variables, VariableHeuristic variableHeur, ValueHeuristic valueHeur, CSPManager manager){
        this.manager = manager;
        this.variableHeuristic = variableHeur;
        this.valueHeuristic = valueHeur;
        this.variableList = variables;
        this.unassigned = new ArrayList<Variable>(variables); // A shallow copy - all items point to the original
    }

    private void _undoRemoveInconsistentValues(Map<Variable, List<Integer>> map){
        for (Map.Entry<Variable, List<Integer>> neighbor : map.entrySet()){
            for (Integer value : neighbor.getValue()){
                neighbor.getKey().addLegalValue(value);
            }
        }
    }

    private boolean _couldSatisfyConstraint(Constraint cons, Queue<Variable> remainingVariables){
        if (remainingVariables.size() == 0)
            return !cons.isViolated(); // The constraint is not violated!
        //Copy, so we don't ruin;
        remainingVariables = new LinkedList<Variable>(remainingVariables);
        Variable next_var = remainingVariables.remove();
        Integer originalValue = next_var.getStartValue();
        for (Integer value : next_var.getLegalValues()){
            next_var.setStartValue(value);
            if (_couldSatisfyConstraint(cons, remainingVariables)) {
                // Return to the original value
                next_var.setStartValue(originalValue);
                return true;
            }
        }
        // Return to the original value
        next_var.setStartValue(originalValue);
        return false;
    }

    // For every value in the domain, checks if there is any assignment s.t. the constraint would not be violated.
    private List<Integer> _removeInconsistentValue(Variable source, Constraint cons){
        Queue<Variable> others = new LinkedList<Variable>(cons.getAffectedVariables());
        others.remove(source);
        Integer previousX = source.getStartValue();
        List<Integer> removed = new LinkedList<Integer>();
        for (Integer x : source.getLegalValues()){
            // Note that there is not harm in resetting the variables - legal values is the only value if only
            // such value possible.
            source.setStartValue(x);
            if (!_couldSatisfyConstraint(cons, others)){
                removed.add(x);
                source.removeLegalValue(x);
            }
        }
        source.setStartValue(previousX);
        return removed;
    }

    // Returns a map of removed values, if detected inconsistency, returns null.
    private Map<Variable, List<Integer>> generalizedArcConsistency(){
        Map<Variable, List<Integer>> inconsistentValues = new HashMap<Variable, List<Integer>>();
        Queue<Arc> arcs = new LinkedList<Arc>();
        // TODO: should arc consistency be only on changed nodes? (Then we get as variable the relevant variable?)
        for (Variable first : variableList){
            for (Constraint cons : first.getConstraints())
                arcs.add(new Arc(first, cons));
        }

        while (!arcs.isEmpty()){
            Arc arc = arcs.remove();

            // Go over different instantiation and check if the relevant constraint is violated
            List<Integer> values = _removeInconsistentValue(arc.source, arc.constaint);

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
                    _undoRemoveInconsistentValues(inconsistentValues);
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


    /**
     * Implementing backtracking recursively to solve CSV, using the variable heuristic and
     * constraint heuristic.
     * @return whether the assignment succeeded or not
     */
    public boolean backtracking(){
        if (unassigned.size() == 0) return true;
        Variable to_assign = variableHeuristic.select(unassigned);
        System.out.println(to_assign);
        unassigned.remove(to_assign);
        for (int num : valueHeuristic.order(to_assign)){
            // No need to check if variable is consistent - arc consistency is taking care of it
            to_assign.setStartValue(num);
            manager.display(getVariables());
            // We assigned - we need to check for consistency
            Map<Variable, List<Integer>> inconsistentValues = generalizedArcConsistency();
            // Arc consistency found legal assignments
            if (inconsistentValues != null) {
                // Recursively continue the enumeration
                if (backtracking())
                    return true;
            }
            // Undo arcConsistency - after coming back from the backtracking
            _undoRemoveInconsistentValues(inconsistentValues);
        }
        to_assign.setStartValue(null);
        unassigned.add(to_assign);
        return false;
    }

    List<Variable> getVariables(){
        // TODO: maybe we want a deep copy?
        return variableList;
    }
}
