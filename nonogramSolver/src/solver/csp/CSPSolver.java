package solver.csp;

import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;

import java.util.*;

/**
 * Solves the CSP at hand using ArcConsistency and backtracking.
 */
public class CSPSolver {
    List<Variable> variableList;
    List<Variable> unassigned;
    VariableHeuristic variableHeuristic; // Which variable to choose next
    ValueHeuristic valueHeuristic; // At what order we assign the values to the variable (startValues)
    ConstraintHandler handler;
    Manager manager;

    //constructor
    public CSPSolver(List<Variable> variables, VariableHeuristic variableHeur, ValueHeuristic valueHeur, ConstraintHandler handler, Manager manager){
        this.manager = manager;
        this.handler = handler;
        this.variableHeuristic = variableHeur;
        this.valueHeuristic = valueHeur;
        this.variableList = variables;
        this.unassigned = null;
    }


    public boolean solve(){
        unassigned = new ArrayList<Variable>(variableList);
        if (handler.initialize(variableList)) {
            return backtracking();
        }
        return false;
    }

    /**
     * Implementing backtracking recursively to solve CSV, using the variable heuristic and
     * constraint heuristic.
     * @return whether the assignment succeeded or not
     */
    public boolean backtracking(){
        if (unassigned.size() == 0) return true;
        if (manager.isStopped()) return false;
        Variable to_assign = variableHeuristic.select(unassigned);
        System.out.println(to_assign);
        unassigned.remove(to_assign);
        for (Object num : valueHeuristic.order(to_assign)){
            // No need to check if variable is consistent - arc consistency is taking care of it
            if (to_assign.isLegalValue(num)) {
                to_assign.setValue(num);
                manager.display(getVariables());
                // We assigned - we need to check for consistency
                Map<Variable, List<Object>> removedValues = handler.checkConstraints(to_assign);
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

    public List<Variable> getVariables(){
        return variableList;
    }
}
