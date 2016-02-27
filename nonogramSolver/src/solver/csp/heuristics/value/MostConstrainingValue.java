package solver.csp.heuristics.value;

import solver.csp.Constraint;
import solver.csp.Variable;
import solver.csp.heuristics.value.ValueHeuristic;

import java.util.*;

/**
 * Created by Zohar on 26/02/2016.
 */
public class MostConstrainingValue implements ValueHeuristic {
    @Override
    public List<Object> order(Variable to_assign) {
            if (to_assign.getValue() != null){
                throw new RuntimeException("Variable must be null upon valueHeuristic");
            }
            Set<Variable> varList = new HashSet<Variable>();
            for (Constraint constr: to_assign.getConstraints()){
                varList.addAll(constr.getAffectedVariables());
            }
            final Map<Object, Integer> valuesConstraining = new HashMap<Object, Integer>();
            for (Object val : to_assign.getLegalValues()){
                to_assign.setValue(val);
                int legalValues = 0;
                for (Variable varNeighbor: varList){
                    for (Object valueNeighbor: varNeighbor.getLegalValues()){
                        if (varNeighbor.isLegalValue(valueNeighbor)){
                            legalValues++;
                        }
                    }
                }
                valuesConstraining.put(val, legalValues);
            }
            to_assign.setValue(null);
            ArrayList<Object> values = new ArrayList<Object>(to_assign.getLegalValues());
            Collections.sort(values, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return valuesConstraining.get(o2) - valuesConstraining.get(o1);
                }
            });
            return values;
        }
}
