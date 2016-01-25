package solver.csp;

import solver.csp.heuristics.ConstraintHeuristic;
import solver.csp.heuristics.VariableHeuristic;

import java.util.*;

/**
 * Solves the CSP at hand using ArcConsistency and backtracking.
 */
public class CSPSolver {
    private class Arc{
        public Variable source;
        public Variable destination;
        public Constraint edge;

        public Arc(Variable source, Variable dest, Constraint edge){
            this.source = source;
            this.destination = dest;
            this.edge = edge;
        }
    }

    List<Variable> variableList;
    List<Variable> unassigned;
    VariableHeuristic variableHeuristic;
    ConstraintHeuristic constraintHeuristic;

    //constructor
    public CSPSolver(List<Variable> variables, VariableHeuristic variableHeur, ConstraintHeuristic constraintHeur){
        this.variableHeuristic = variableHeur;
        this.constraintHeuristic = constraintHeur;
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

    // For every value in the domain, checks if there is any assignment s.t. the constraint would not be violated.
    private List<Integer> _removeInconsistentValue(Variable source, Variable dest, Constraint cons){
        Integer previousX = source.getStartValue();
        Integer previousY = dest.getStartValue();
        List<Integer> removed = new LinkedList<Integer>();
        for (Integer x : source.getLegalValues()){
            // Note that there is not harm in resetting the variables - legal values is the only value if only
            // such value possible.
            boolean possibleAssignment = false;
            source.setStartValue(x);
            for (Integer y : dest.getLegalValues()){
                dest.setStartValue(y);
                if (!cons.isViolated()){
                    possibleAssignment = true;
                }
            }
            if (!possibleAssignment){
                removed.add(x);
                source.removeLegalValue(x);
            }
        }
        source.setStartValue(previousX);
        dest.setStartValue(previousY);
        return removed;
    }

    // Returns a map of removed values, if detected inconsistency, returns null.
    private Map<Variable, List<Integer>> arcConsistency(){
        Map<Variable, List<Integer>> inconsistentValues = new TreeMap<Variable, List<Integer>>();
        Queue<Arc> arcs = new LinkedList<Arc>();
        // TODO: should arc consistency be only on changed nodes? (Then we get as variable the relevant variable?)
        for (Variable first : variableList){
            for (Constraint cons : first.getConstraints())
                for (Variable second : cons.getAffectedVariables())
                    if (first != second)
                        arcs.add(new Arc(first, second, cons));
        }

        while (!arcs.isEmpty()){
            Arc arc = arcs.remove();

            // Go over different instantiation and check if the relevant constraint is violated
            List<Integer> values = _removeInconsistentValue(arc.source, arc.destination, arc.edge);

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
                            arcs.add(new Arc(first, arc.source, cons));
            }
        }
        return inconsistentValues;
    }


    /**
     * Implemnting backtracking recursively to solve CSV, using the variable heuristic and
     * constraint heuristic.
     * @return whether the assignment succeeded or not
     */
    public boolean backtracking(){
        if (unassigned.size() == 0) return true;
        Variable to_assign = variableHeuristic.select(unassigned);
        for (int num : constraintHeuristic.order(to_assign.getLegalValues())){
            // No need to check if variable is consistent - arc consistency is taking care of it
            to_assign.setStartValue(num);
            // We assigned - we need to check for consistency
            Map<Variable, List<Integer>> inconsistentValues = arcConsistency();
            // There is a node with no domain
            if (inconsistentValues == null)
                return false;
            // Recursively continue the enumeration
            if (backtracking())
                return true;
            // Undo arcConsistency - after coming back from the backtracking
            _undoRemoveInconsistentValues(inconsistentValues);
            to_assign.removeLegalValue(num);
        }
        return false;
    }

    List<Variable> getVariables(){
        // TODO: maybe we want a deep copy?
        return variableList;
    }
}
