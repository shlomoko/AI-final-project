package solver.gui;


import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Grid extends GridPane {
    private int width;
    private int height;
    private Rectangle[][] rectangles;

    public Grid(int width, int height){
        this.setGridLinesVisible(true);
        this.setHgap(1);
        this.setVgap(1);
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
                this.add(rectangles[i][j],i,j);
            }
        }
    }

    public void setColor(Color[][] colors){
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
                rectangles[i][j].setFill(colors[i][j]);
            }
        }
    }
}
