package solver.csp.heuristics;

import solver.csp.CSPSolver;
import solver.csp.Constraint;
import solver.csp.Variable;

import java.util.*;

/**
 * Created by Shlomo on 26/01/2016.
 */
public class LeastConstriningValue implements ValueHeuristic {
    public List<Integer> order (Variable variable){
        //constraints = variable.getConstraints();
        if (variable.getStartValue() != null){
            throw new RuntimeException("Variable must be null upon valueHeuristic");
        }
        Set<Variable> varList = new HashSet<Variable>();
        for (Constraint constr: variable.getConstraints()){
            varList.addAll(constr.getAffectedVariables());
        }
        final Map<Integer, Integer> valuesConstraining = new TreeMap<Integer, Integer>();
        for (int val : variable.getLegalValues()){
            variable.setStartValue(val);
            int legalValues = 0;
            for (Variable varNeighbor: varList){
                for (Integer valueNeighbor: varNeighbor.getLegalValues()){
                    if (varNeighbor.isLegalValue(valueNeighbor)){
                        legalValues++;
                    }
                }
            }
            valuesConstraining.put(val, legalValues);
        }
        variable.setStartValue(null);
        ArrayList<Integer> values = new ArrayList<Integer>(variable.getLegalValues());
        Collections.sort(values, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return valuesConstraining.get(o1) - valuesConstraining.get(o2);
            }
        });
        return values;
    }

}
