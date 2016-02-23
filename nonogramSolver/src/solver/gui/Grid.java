package solver.gui;


import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Grid extends GridPane {
    private int width;
    private int height;
    private Rectangle[][] rectangles;

    public Grid(int width, int height){
        this.setGridLinesVisible(true);
        this.setHgap(1);
        this.setVgap(1);
        ColumnConstraints columnCons = new ColumnConstraints();
        columnCons.setHalignment(HPos.RIGHT);
        this.getColumnConstraints().add(columnCons);
        RowConstraints rowCons = new RowConstraints();
        rowCons.setValignment(VPos.BOTTOM);
        this.getRowConstraints().add(rowCons);
        setSize(width, height);
    }

    public int getColumns(){
        return width;
    }

    public int getRows(){
        return height;
    }

    public void setSize(int width, int height){
        this.getChildren().clear();
        this.width = width;
        this.height = height;

        rectangles = new Rectangle[width][height];
        for (int i = 0; i<width; i++) {
            for (int j = 0; j < height; j++) {
                rectangles[i][j] = new Rectangle(20, 20, Color.GRAY);
                this.add(rectangles[i][j],i+1,j+1);
            }
        }
    }

    public void setHint(int[][] cols, int[][] rows){
        for (int i=0; i<width; i++){
            this.add(new Text(_join(cols[i], "\n")), i+1, 0);
        }
        for (int j=0; j<height; j++){
            this.add(new Text(_join(rows[j], " ")), 0, j+1);
        }
    }

    private String _join(int[] arr, String sep){
        if (arr.length == 0) return "";
        String text = "" + arr[0];
        for (int k=1; k<arr.length; k++){
            text += sep + arr[k];
        }
        return text;
    }

    public void setColor(Color[][] colors){
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
                rectangles[i][j].setFill(colors[i][j]);
            }
        }
    }
}
