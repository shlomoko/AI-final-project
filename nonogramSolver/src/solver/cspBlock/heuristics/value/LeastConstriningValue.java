package solver.cspBlock.heuristics.value;

import solver.Constraint;
import solver.ValueHeuristic;
import solver.Variable;
import solver.cspBlock.BlockVariable;

import java.util.*;

/**
 * Created by Shlomo on 26/01/2016.
 */
public class LeastConstriningValue implements ValueHeuristic {
    public List<Object> order (Variable blockVariable){
        //constraints = variable.getConstraints();
        if (blockVariable.getValue() != null){
            throw new RuntimeException("Variable must be null upon valueHeuristic");
        }
        Set<Variable> varList = new HashSet<Variable>();
        for (Constraint constr: blockVariable.getConstraints()){
            varList.addAll(constr.getAffectedVariables());
        }
        final Map<Object, Integer> valuesConstraining = new HashMap<Object, Integer>();
        for (Object val : blockVariable.getLegalValues()){
            blockVariable.setValue(val);
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
        blockVariable.setValue(null);
        ArrayList<Object> values = new ArrayList<Object>(blockVariable.getLegalValues());
        Collections.sort(values, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return valuesConstraining.get(o1) - valuesConstraining.get(o2);
            }
        });
        return values;
    }

}
