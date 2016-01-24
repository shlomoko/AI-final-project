package solver.csp;

import solver.csp.heuristics.ConstraintHeuristic;
import solver.csp.heuristics.VariableHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Solves the CSP at hand using ArcConsistency and backtracking.
 */
public class CSPSolver {
    List<Variable> variableList;
    List<Variable> unassigned;
    VariableHeuristic variableHeuristic;
    ConstraintHeuristic constraintHeuristic;

    public CSPSolver(List<Variable> variables, VariableHeuristic variableHeur, ConstraintHeuristic constraintHeur){
        this.variableHeuristic = variableHeur;
        this.constraintHeuristic = constraintHeur;
        this.variableList = variables;
        this.unassigned = new ArrayList<Variable>(variables); // A shallow copy - all items point to the original
    }

    public Map<Variable, List<Integer>> arcConsistency(){
        return null;
    }

    /**
     * Implemnting backtracking recursively to solve CSV, using the variable heuristic and
     * constraint heuristic.
     * @return whether the assignment succeeded or not
     */
    public boolean backTracking(){
        if (unassigned.size() == 0) return true;
        Variable to_assign = variableHeuristic.select(unassigned);
        for (int num : constraintHeuristic.order(to_assign.getPossibleValues())){
            // No need to check if variable is consistent - arc consistency is taking care of it
            to_assign.setStartValue(num);
            // We assigned - we need to check for consistency
            Map<Variable, List<Integer>> inconsistentValues = arcConsistency();
            // Recursively continue the enumeration
            if (backTracking())
                return true;
            // Undo arcConsistency
            for (Map.Entry<Variable, List<Integer>> neighbor : inconsistentValues.entrySet()){
                for (Integer value : neighbor.getValue()){
                    neighbor.getKey().addLegalValue(value);
                }
            }
            to_assign.removeLegalValue(num);
        }
        return false;
    }

    List<Variable> getVariables(){
        // TODO: maybe we want a deep copy?
        return variableList;
    }
}
