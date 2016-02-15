package solver.cspBlock.heuristics.variable;

import solver.cspBlock.Variable;
import solver.cspBlock.heuristics.variable.VariableHeuristic;

import java.util.*;

//TODO zohar
public class MaxSumVariableHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<Variable> unassigned) {
        Map<Integer,Integer> rowSums = new HashMap<Integer, Integer>();
        Map<Integer,Integer> colSums = new HashMap<Integer, Integer>();
        Map<Integer,List<Variable>> rowVariables = new HashMap<Integer, List<Variable>>();
        Map<Integer,List<Variable>> colVariables = new HashMap<Integer, List<Variable>>();
        for (Variable var : unassigned){
            int index = var.getIndex();
            if(var.isRow()){
                if(rowSums.containsKey(index)){
                    rowSums.put(index, rowSums.get(index) + var.getLength());
                }else{
                    rowSums.put(index, var.getLength());
                }
                upadateMap(rowVariables, index,var);

            }else{
                if(colSums.containsKey(index)){
                    colSums.put(index, colSums.get(index) + var.getLength());
                }else{
                    colSums.put(index, var.getLength());
                }
                upadateMap(colVariables, index,var);
            }
        }

        int maxRowValue = 0;
        int maxRowIndex = -1;

        int maxColValue = 0;
        int maxColIndex = -1;

        for (Integer key : rowSums.keySet()){
            if(rowSums.get(key) >= maxRowValue){
                maxRowValue = rowSums.get(key);
                maxRowIndex = key;
            }
        }

        for (Integer key : colSums.keySet()){
            if(colSums.get(key) >= maxColValue){
                maxColValue = colSums.get(key);
                maxColIndex = key;
            }
        }

        Map<Integer, List<Variable>>  maxMap = maxColValue > maxRowValue? colVariables : rowVariables;
        int maxIndex = maxColValue > maxRowValue ? maxColIndex : maxRowIndex;
        List<Variable> maxList = maxMap.get(maxIndex);

        Variable maxVariable = Collections.max(maxList, new Comparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                return o1.getLength() - o2.getLength();
            }
        });

        return maxVariable;
    }

    private void upadateMap(Map<Integer, List<Variable>> varList, int index, Variable var){
        if(varList.containsKey(index)){
            List<Variable> list = varList.get(index);
            list.add(var);
            varList.put(index, list);
        }else{
            List<Variable> list = new ArrayList<Variable>();
            list.add(var);
            varList.put(index, list);
        }
    }
}
