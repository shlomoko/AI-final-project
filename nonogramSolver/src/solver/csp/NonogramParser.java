package solver.csp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class NonogramParser {
    private List<List<Integer>> rowVariables;
    private List<List<Integer>> colVariables;
    private int[][] rowHints;
    private int[][] colHints;
    private int rowDim;
    private int colDim;

    public NonogramParser(File file) throws IOException {
        this.readFile(file);
        rowHints = _convertListToMatrix(rowVariables);
        colHints = _convertListToMatrix(colVariables);
    }

    public int getRows() {
        return rowVariables.size();
    }

    public int getColumns() {
        return colVariables.size();
    }

    private int[][] _convertListToMatrix(List<List<Integer>> list){
        int[][] hints = new int[list.size()][];
        for (int i = 0; i<list.size(); i++){
            List<Integer> inner = list.get(i);
            hints[i] = new int[inner.size()];
            for (int j=0; j<inner.size(); j++) {
                hints[i][j] = inner.get(j);
            }
        }
        return hints;
    }

    public int[][] getRowHints() {
        return rowHints;
    }

    public int[][] getColumnHints() {
        return colHints;
    }

    public List<List<Integer>> getRowsList(){
        return rowVariables;
    }

    public List<List<Integer>> getColumnList(){
        return colVariables;
    }

    public int getRowAmount(){
        return rowDim;
    }

    public int getColAmount(){
        return colDim;
    }

    private List<List<Integer>> _readNumberLists(BufferedReader file, int size) throws IOException {
        List<List<Integer>> res = new ArrayList<List<Integer>>();
        for (int i = 0; i < size; i++) {
            String[] rowNumbers = file.readLine().split(",");
            List<Integer> numbers = new ArrayList<Integer>();
            for (String number : rowNumbers) {
                numbers.add(Integer.parseInt(number));
            }
            res.add(numbers);
        }
        return res;
    }

    private void readFile(File fileName) throws IOException {
        this.colVariables = new ArrayList<List<Integer>>();
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = file.readLine()) != null) {
            if (line.equals("[Dimensions]")) {
                rowDim = Integer.parseInt(file.readLine());
                colDim = Integer.parseInt(file.readLine());
            } else if (line.equals("[Row clues]")) {
                this.rowVariables = _readNumberLists(file, rowDim);
            } else if (line.equals("[Column clues]")) {
                this.colVariables = _readNumberLists(file, colDim);
            } else if (line.equals("[Solution]")) {
                file.close();
                return;
            }

        }
        file.close();
        if (rowVariables == null || colVariables == null){
            throw new IOException("Invalid File");
        }
    }
}
