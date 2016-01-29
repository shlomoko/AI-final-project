package solver.csp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public class CSPParser {
    private  List<List<Variable>> rowVariables;
    private  List<List<Variable>> colVariables;
    private List<Variable> variables;
    int rowDim;
    int colDim;


    public List<Variable> parser(String fileName){
        this.readFile(fileName);
        //Add Order Constraint to the variables
        addOrderConstraint(true);
        addOrderConstraint(false);

        //Add Last Block Constraint to the variables
        addLastBlockConstraint(true);
        addLastBlockConstraint(false);

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

        return variables;
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

    private void addLastBlockConstraint(boolean isRow){
        int dim = isRow ? rowDim : colDim;
        List<List<Variable>> variableList = isRow ? rowVariables : colVariables;
        for (int i = 0; i< dim; i++){
            List<Variable> currRow = variableList.get(i);
            Constraint constraint = new LastBlockConstraint(currRow.get(currRow.size()-1), dim);
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
                }else if (line.equals("[Row clues]")){
                    for (int i = 0; i< rowDim; i++){
                        String[] rowNumbers = file.readLine().split(",");
                        rowVariables.add(new ArrayList<Variable>());
                        for (int j=0; j<rowNumbers.length; j++){
                            Variable var = new Variable(Integer.parseInt(rowNumbers[j]),true, j, rowDim);
                            rowVariables.get(i).add(var);
                            variables.add(var);
                        }
                    }
                }else if(line.equals("[Column clues]")){
                    for (int i = 0; i< colDim; i++){
                        String[] colNumbers = file.readLine().split(",");
                        colVariables.add(new ArrayList<Variable>());
                        for (int j=0; j<colNumbers.length; j++){
                            Variable var = new Variable(Integer.parseInt(colNumbers[j]),false, j, colDim);
                            colVariables.get(i).add(var);
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