package solver.csp;

import java.util.ArrayList;

/**
 * Intersection constraint - check if a cell has a specific color
 */
public class IntersectConstraint implements Constraint {
    ArrayList<Variable> rowBlocks;
    ArrayList<Variable> columnBlocks;
    Integer row;
    Integer column;

    /**
     * Create a new intersection constraint - checks if block intersect between row
     * and column
     * @param row the row index of the constraint
     * @param column the column index of the constraint
     * @param rowBlocks the blocks in the specified row
     * @param columnBlocks the columns in the specified columns
     */
    public IntersectConstraint(int row, int column, ArrayList<Variable> rowBlocks, ArrayList<Variable> columnBlocks){
        this.rowBlocks = rowBlocks;
        this.columnBlocks = columnBlocks;
        this.row = row;
        this.column = column;
    }

    // Search using binary search to find the first index before the variable.
    private Variable _binarySearchBlocks(ArrayList<Variable> block, int num){
        int lower = 0;
        int upper = block.size() - 1;
        while (upper > lower){
            int mid = (upper + lower)/2;
            if (block.get(mid).getStartValue() < num){
                lower = mid;
            } else if (block.get(mid).getStartValue() > num) {
                upper = mid;
            } else {
                return block.get(mid);
            }
        }
        return block.get(lower);
    }

    @Override
    public boolean isViolated() {
        Variable rowCandidate = _binarySearchBlocks(rowBlocks, row);
        Variable columnCandidate = _binarySearchBlocks(rowBlocks, row);
        boolean isRowMarked = rowCandidate.getStartValue() + rowCandidate.getLength() > row;
        boolean isColumnMarked = columnCandidate.getStartValue() + columnCandidate.getLength() > column;
        return (isRowMarked && isColumnMarked) || (!isRowMarked && !isColumnMarked);
    }
}
