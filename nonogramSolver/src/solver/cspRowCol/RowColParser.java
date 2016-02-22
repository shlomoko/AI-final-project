package solver.cspRowCol;

import solver.Variable;
import solver.cspRowCol.constraints.RowColConstraint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Gets a file of a puzzle and creates Variables list accordingly.
 */
public class RowColParser {
    private  List<RowColVariable> rowVariables;
    private  List<RowColVariable> colVariables;
    private List<RowColVariable> rowColVariables;
    int rowDim;
    int colDim;
    int[][] rowHints;
    int[][] colHints;


    public RowColParser(String fileName){
        this.readFile(fileName);

        for (RowColVariable rowVar : rowVariables){
            for (RowColVariable colVar : colVariables){
                RowColConstraint constraint = new RowColConstraint(rowVar, colVar);
                rowVar.addConstraint(constraint);
                colVar.addConstraint(constraint);
            }
        }
    }

    public List<Variable> getVariables(){
        return new ArrayList<Variable>(rowColVariables);
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


    private void readFile(String fileName){
        this.rowVariables = new ArrayList<RowColVariable>();
        this.colVariables = new ArrayList<RowColVariable>();
        this.rowColVariables = new ArrayList<RowColVariable>();
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
                        List<Integer> numbers = new ArrayList<Integer>();
                        for (String number : rowNumbers) {
                            numbers.add( Integer.parseInt(number));
                        }
                        rowHints[i] = new int[numbers.size()];
                        for (int j = 0; j < numbers.size(); j++) {
                            rowHints[i][j] = numbers.get(j);
                        }

                        RowColVariable var = new RowColVariable(numbers,true, i, colDim);
                        rowVariables.add(var);
                        rowColVariables.add(var);
                        }
                }else if(line.equals("[Column clues]")){
                    for (int i = 0; i< colDim; i++){
                        String[] colNumbers = file.readLine().split(",");
                        List<Integer> numbers = new ArrayList<Integer>();
                        for (String number : colNumbers) {
                            numbers.add( Integer.parseInt(number));
                        }
                        colHints[i] = new int[numbers.size()];
                        for (int j = 0; j < numbers.size(); j++) {
                            colHints[i][j] = numbers.get(j);
                        }

                        RowColVariable var = new RowColVariable(numbers,false, i, rowDim);
                        rowColVariables.add(var);
                        colVariables.add(var);
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