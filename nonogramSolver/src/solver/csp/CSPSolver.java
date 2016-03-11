package solver.csp;

import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.gui.Counters;

import java.util.*;

/**
 * Solves the CSP at hand using ArcConsistency and backtracking.
 */
public class CSPSolver {
    List<? extends Variable> variableList;
    List<Variable> unassigned;
    VariableHeuristic variableHeuristic; // Which variable to choose next
    ValueHeuristic valueHeuristic; // At what order we assign the values to the variable (startValues)
    ConstraintHandler handler;
    Manager manager;

    //constructor
    public CSPSolver(List<? extends Variable> variables, VariableHeuristic variableHeur, ValueHeuristic valueHeur, ConstraintHandler handler, Manager manager){
        this.manager = manager;
        this.handler = handler;
        this.variableHeuristic = variableHeur;
        this.valueHeuristic = valueHeur;
        this.variableList = variables;
        this.unassigned = null;
    }


    public boolean solve(){
        manager.report("Starting solving using " + manager.getClass().getSimpleName() +
                        ", " + variableHeuristic.getClass().getSimpleName() + ", " +
                        valueHeuristic.getClass().getSimpleName() + ", and " +
                        handler.getClass().getSimpleName());
        unassigned = new ArrayList<Variable>(variableList);
        Counters counters = Counters.getInstance();
        counters.tick("total run");
        if (handler.initialize(variableList)) {
            manager.report("Starting backtracking");
            boolean result = backtracking();
            counters.tock("total run");
            return result;
        }
        counters.tock("total run");
        return false;
    }

    /**
     * Implementing backtracking recursively to solve CSV, using the variable heuristic and
     * constraint heuristic.
     * @return whether the assignment succeeded or not
     */
    public boolean backtracking(){
        if (unassigned.size() == 0) return true;
        Variable to_assign = variableHeuristic.select(unassigned);
        unassigned.remove(to_assign);
        for (Object num : valueHeuristic.order(to_assign)){
            if (manager.isStopped()) return false;
            // No need to check if variable is consistent - arc consistency is taking care of it
            if (to_assign.isLegalValue(num)) {
                to_assign.setValue(num);
                Counters.getInstance().addCount("Assignments", 1L   );
                manager.display();
                // We assigned - we need to check for consistency
                Map<? extends Variable, List<Object>> removedValues = handler.checkConstraints(to_assign);
                // Arc consistency found legal assignments
                if (removedValues != null) {
                    // Recursively continue the enumeration
                    if (backtracking())
                        return true;
                }
                // Undo arcConsistency - after coming back from the backtracking
                handler.restoreValue(removedValues);
            }
        }
        to_assign.setValue(null);
        unassigned.add(to_assign);
        return false;
    }

    public List<? extends Variable> getVariables(){
        return variableList;
    }
}
