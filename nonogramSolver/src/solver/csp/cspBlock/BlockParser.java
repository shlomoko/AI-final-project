package solver.csp.cspBlock;

import solver.csp.Constraint;
import solver.csp.Variable;
import solver.csp.cspBlock.constraints.IntersectConstraint;
import solver.csp.cspBlock.constraints.OrderConstraint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Gets a file of a puzzle and creates Variables list accordingly.
 */
public class BlockParser {
    private  List<List<BlockVariable>> rowVariables;
    private  List<List<BlockVariable>> colVariables;
    private List<BlockVariable> blockVariables;
    int rowDim;
    int colDim;
    int[][] rowHints;
    int[][] colHints;


    public BlockParser(String fileName){
        this.readFile(fileName);
        //Add Order Constraint to the variables
        addOrderConstraint(true);
        addOrderConstraint(false);


        //Add rowSum attribute
        addRowSum(true);
        addRowSum(false);

        //Add Last Block Constraint to the variables
        //addLastBlockConstraint(true);
        //addLastBlockConstraint(false);

        //Add Intersect Constraint to the variables
        for(int i=0; i<rowDim; i++){
            for (int j=0; j<colDim; j++){
                List<BlockVariable> currRow = rowVariables.get(i);
                List<BlockVariable> currCol = colVariables.get(j);
                Constraint constraint = new IntersectConstraint(i, j, currRow, currCol);
                for(BlockVariable rowVar : currRow){
                    rowVar.addConstraint(constraint);
                }
                for(BlockVariable colVar :currCol){
                    colVar.addConstraint(constraint);
                }
            }
        }
    }

    public List<Variable> getVariables(){
        return new LinkedList<Variable>(blockVariables);
    }

    public int getRows(){
        return rowDim;
    }

    public int getColumns(){
        return colDim;
    }

    public int[][] getRowHints(){
        return rowHints;
    }

    public int[][] getColumnHints(){
        return colHints;
    }

    private void addOrderConstraint(boolean isRow){
        int dim = isRow ? rowDim : colDim;
        List<List<BlockVariable>> variableList = isRow ? rowVariables : colVariables;
        for (int i = 0; i< dim; i++){
            List<BlockVariable> currRow = variableList.get(i);
            for(int j=0; j<currRow.size()-1; j++){
                Constraint constraint = new OrderConstraint(currRow.get(j), currRow.get(j+1));
                currRow.get(j).addConstraint(constraint);
                currRow.get(j+1).addConstraint(constraint);
            }
        }
    }

    private void addRowSum(boolean isRow){
        final Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
        List<Integer> rowSum = new ArrayList<Integer>();
        int dim = isRow ? rowDim : colDim;
        int otherDim = isRow ? colDim : rowDim;
        List<List<BlockVariable>> variableList = isRow ? rowVariables : colVariables;
        List<List<BlockVariable>> otherVariableList = isRow ? colVariables : rowVariables;
        for (int i = 0; i< dim; i++){
            List<BlockVariable> currRow = variableList.get(i);
            int sum = 0;
            for (BlockVariable var : currRow){
                sum += var.getLength();
            }
            rowMap.put(i,sum);
            rowSum.add(i);
        }
        Collections.sort(rowSum, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return rowMap.get(o2) - rowMap.get(o1);
            }
        });

        for (int i=0; i<otherDim; i++) {
            List<BlockVariable> currRow = otherVariableList.get(i);
            for (BlockVariable var : currRow) {
                var.addRowSum(rowSum);
            }
        }


    }


    private void readFile(String fileName){
        this.rowVariables = new ArrayList<List<BlockVariable>>();
        this.colVariables = new ArrayList<List<BlockVariable>>();
        this.blockVariables = new ArrayList<BlockVariable>();
        this.rowDim = 0;
        this.colDim = 0;
        try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = file.readLine()) != null) {
                if (line.equals("[Dimensions]")){
                    rowDim =  Integer.parseInt(file.readLine());
                    colDim =  Integer.parseInt(file.readLine());
                    rowHints = new int[rowDim][];
                    colHints = new int[colDim][];
                }else if (line.equals("[Row clues]")){
                    for (int i = 0; i< rowDim; i++){
                        String[] rowNumbers = file.readLine().split(",");
                        rowHints[i] = new int[rowNumbers.length];
                        rowVariables.add(new ArrayList<BlockVariable>());
                        for (int j=0; j<rowNumbers.length; j++){
                            int value = Integer.parseInt(rowNumbers[j]);
                            BlockVariable var = new BlockVariable(value,true, i, colDim - value + 1);
                            rowHints[i][j] = value;
                            rowVariables.get(i).add(var);
                            blockVariables.add(var);
                        }
                    }
                }else if(line.equals("[Column clues]")){
                    for (int i = 0; i< colDim; i++){
                        String[] colNumbers = file.readLine().split(",");
                        colHints[i] = new int[colNumbers.length];
                        colVariables.add(new ArrayList<BlockVariable>());
                        for (int j=0; j<colNumbers.length; j++){
                            int value = Integer.parseInt(colNumbers[j]);
                            BlockVariable var = new BlockVariable(value,false, i, rowDim - value + 1);
                            colHints[i][j] = value;
                            colVariables.get(i).add(var);
                            blockVariables.add(var);
                        }
                    }
                }else if (line.equals("[Solution]")){
                    file.close();
                    return;
                }

            }
            file.close();
        } catch (IOException e) {
            System.out.println("IO exception");
        }

    }
}