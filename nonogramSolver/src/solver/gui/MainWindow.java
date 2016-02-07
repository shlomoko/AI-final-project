package solver.gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import solver.csp.CSPManager;
import solver.csp.heuristics.DegreeHeuristic;
import solver.csp.heuristics.MinimumRemainingValues;
import solver.csp.heuristics.ValueHeuristic;
import solver.csp.heuristics.VariableHeuristic;

import java.io.File;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class MainWindow extends Application {
    Grid grid;
    CSPManager manager;
    ChoiceBox<ValueHeuristicsEnum> valueHeuristics;
    ChoiceBox<VariableHeuristicsEnum> variableHeuristics;

    private enum ValueHeuristicsEnum {
        UNDEFINED("undefined");

        private String label;

        ValueHeuristicsEnum(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }

    private enum VariableHeuristicsEnum {
        DEGREE("Degree"),
        MRV("Minumum Remaining Value");

        private String label;

        VariableHeuristicsEnum(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        grid = new Grid(10, 10);
        GridPane root = new GridPane();
        root.addRow(0, grid);
        root.setVgap(10);

        Pane buttons = new HBox();
        valueHeuristics = new ChoiceBox<ValueHeuristicsEnum>();
        valueHeuristics.getItems().setAll(ValueHeuristicsEnum.values());
        valueHeuristics.setValue(ValueHeuristicsEnum.UNDEFINED);
        variableHeuristics = new ChoiceBox<VariableHeuristicsEnum>();
        variableHeuristics.getItems().setAll(VariableHeuristicsEnum.values());
        variableHeuristics.setValue(VariableHeuristicsEnum.DEGREE);
        Button btn = new Button("Load File");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                /*Color[][] colors = new Color[10][10];
                for (int i=0; i<10; i++){
                    for (int j = 0; j<10; j++){
                        colors[i][j] = Color.GREEN;
                    }
                }
                grid.setColor(colors);*/
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                File file = fileChooser.showOpenDialog(primaryStage);
                VariableHeuristic varHeur = getChosenVariableHeuristic();
                ValueHeuristic valueHeur = getChosenValueHeurisitic();
                if (valueHeur == null || varHeur == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Invalid Heuristics Chosen");
                    alert.setContentText("Ensure you chose a legal value");
                    alert.showAndWait();
                } else {
                    manager = new CSPManager(file, grid, varHeur, valueHeur);
                }
            }
        });
        buttons.getChildren().addAll(valueHeuristics, variableHeuristics, btn);
        root.addRow(1,buttons);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Nonogram CSP Solver");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    public VariableHeuristic getChosenVariableHeuristic() {
        switch (variableHeuristics.getValue()){
            case DEGREE:
                return new DegreeHeuristic();
            case MRV:
                return new MinimumRemainingValues();
        }
        return null;
    }

    public ValueHeuristic getChosenValueHeurisitic() {
        switch (valueHeuristics.getValue()){
            case UNDEFINED:
                return null;
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
