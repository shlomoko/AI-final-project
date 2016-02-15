package solver.cspRowCol;

import solver.cspBlock.constraints.Constraint;
import solver.cspBlock.constraints.IntersectConstraint;
import solver.cspBlock.constraints.LastBlockConstraint;
import solver.cspBlock.constraints.OrderConstraint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Gets a file of a puzzle and creates Variables list accordingly.
 */
public class CSPParser {
    private  List<List<Variable>> rowVariables;
    private  List<List<Variable>> colVariables;
    private List<Variable> variables;
    int rowDim;
    int colDim;
    int[][] rowHints;
    int[][] colHints;


    public CSPParser(String fileName){
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
                List<Variable> currRow = rowVariables.get(i);
                List<Variable> currCol = colVariables.get(j);
                Constraint constraint = new IntersectConstraint(i, j, currRow, currCol);
                for(Variable rowVar : currRow){
                    rowVar.addConstraint(constraint);
                }
                for(Variable colVar :currCol){
                    colVar.addConstraint(constraint);
                }
            }
        }
    }

    public List<Variable> getVariables(){
        return variables;
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
        List<List<Variable>> variableList = isRow ? rowVariables : colVariables;
        for (int i = 0; i< dim; i++){
            List<Variable> currRow = variableList.get(i);
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
        List<List<Variable>> variableList = isRow ? rowVariables : colVariables;
        List<List<Variable>> otherVariableList = isRow ? colVariables : rowVariables;
        for (int i = 0; i< dim; i++){
            List<Variable> currRow = variableList.get(i);
            int sum = 0;
            for (Variable var : currRow){
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
            List<Variable> currRow = otherVariableList.get(i);
            for (Variable var : currRow) {
                var.addRowSum(rowSum);
            }
        }


    }

    private void addLastBlockConstraint(boolean isRow){
        int dim = isRow ? rowDim : colDim;
        int domainSize = isRow ? colDim : rowDim;
        List<List<Variable>> variableList = isRow ? rowVariables : colVariables;
        for (int i = 0; i< dim; i++){
            List<Variable> currRow = variableList.get(i);
            Constraint constraint = new LastBlockConstraint(currRow.get(currRow.size()-1), domainSize);
            currRow.get(currRow.size()-1).addConstraint(constraint);
        }
    }

    private void readFile(String fileName){
        this.rowVariables = new ArrayList<List<Variable>>();
        this.colVariables = new ArrayList<List<Variable>>();
        this.variables = new ArrayList<Variable>();
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
                        String rowNumbers = file.readLine();
                        Variable var = new Variable(rowNumbers,true, i);
                        variables.add(var);
                        }
                }else if(line.equals("[Column clues]")){
                    for (int i = 0; i< colDim; i++){
                        String colNumbers = file.readLine();
                        Variable var = new Variable(colNumbers,false, i);
                        variables.add(var);
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