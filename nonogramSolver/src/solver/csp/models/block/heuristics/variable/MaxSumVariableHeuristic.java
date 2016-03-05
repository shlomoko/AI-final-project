package solver.csp.models.block.heuristics.variable;

import solver.annotations.Default;
import solver.annotations.LimitTo;
import solver.csp.Variable;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.block.BlockVariable;

import java.util.*;

@Default(1)
@LimitTo("BlockManager")
public class MaxSumVariableHeuristic implements VariableHeuristic {
    @Override
    public Variable select(List<? extends Variable> unassigned) {
        Map<Integer,Integer> rowSums = new HashMap<Integer, Integer>();
        Map<Integer,Integer> colSums = new HashMap<Integer, Integer>();
        Map<Integer,List<BlockVariable>> rowVariables = new HashMap<Integer, List<BlockVariable>>();
        Map<Integer,List<BlockVariable>> colVariables = new HashMap<Integer, List<BlockVariable>>();
        for (Variable basicVar : unassigned){
            BlockVariable var = (BlockVariable) basicVar;
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

        Map<Integer, List<BlockVariable>>  maxMap = maxColValue > maxRowValue? colVariables : rowVariables;
        int maxIndex = maxColValue > maxRowValue ? maxColIndex : maxRowIndex;
        List<BlockVariable> maxList = maxMap.get(maxIndex);

        BlockVariable maxBlockVariable = Collections.max(maxList, new Comparator<BlockVariable>() {
            @Override
            public int compare(BlockVariable o1, BlockVariable o2) {
                return o1.getLength() - o2.getLength();
            }
        });

        return maxBlockVariable;
    }

    private void upadateMap(Map<Integer, List<BlockVariable>> varList, int index, BlockVariable var){
        if(varList.containsKey(index)){
            List<BlockVariable> list = varList.get(index);
            list.add(var);
            varList.put(index, list);
        }else{
            List<BlockVariable> list = new ArrayList<BlockVariable>();
            list.add(var);
            varList.put(index, list);
        }
    }
}
