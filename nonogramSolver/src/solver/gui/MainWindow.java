package solver.gui;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import solver.csp.Manager;
import solver.csp.handlers.ArcConsistency;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.cspBlock.heuristics.variable.BlockLengthAndMaxSumHeuristic;
import solver.csp.cspBlock.heuristics.variable.BlockLengthHeuristic;
import solver.csp.cspBlock.heuristics.variable.MaxSumVariableHeuristic;
import solver.csp.heuristics.variable.DegreeHeuristic;
import solver.csp.heuristics.variable.MinimumRemainingValues;
import solver.csp.cspBlock.BlockManager;
import solver.csp.heuristics.value.LeastConstriningValue;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.cspRowCol.RowColManager;
import solver.measure.Counters;

import java.io.File;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class MainWindow extends Application {
    Grid grid;
    Manager manager;
    String filePath;
    Thread managerThread;
    ChoiceBox<ValueHeuristicsEnum> valueHeuristics;
    ChoiceBox<VariableHeuristicsEnum> variableHeuristics;
    ChoiceBox<ModelEnum> models;

    private enum ModelEnum {
        BLOCK("Block"),
        ROWCOL("Row-Column");

        private String label;

        ModelEnum(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }

    private enum ValueHeuristicsEnum {
        LCV("Least Constraining Value");

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
        MRV("Minumum Remaining Value"),
        LENGTH("Longest Block"),
        MAX_SUM("Max Sum"),
        MAX_SUM_AND_LENGTH("Longest Block and Max Sum");

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
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        grid = new Grid(10, 10);
        VBox root2 = new VBox();
        GridPane root = new GridPane();
        root.addRow(0, grid);
        root.setVgap(10);
        final FileChooser fileChooser = new FileChooser();

        Pane buttons = new HBox();
        valueHeuristics = new ChoiceBox<ValueHeuristicsEnum>();
        valueHeuristics.getItems().setAll(ValueHeuristicsEnum.values());
        valueHeuristics.setValue(ValueHeuristicsEnum.LCV);
        variableHeuristics = new ChoiceBox<VariableHeuristicsEnum>();
        variableHeuristics.getItems().setAll(VariableHeuristicsEnum.values());
        variableHeuristics.setValue(VariableHeuristicsEnum.DEGREE);
        models = new ChoiceBox<ModelEnum>();
        models.getItems().setAll(ModelEnum.values());
        models.setValue(ModelEnum.BLOCK);
        Button btn = new Button("Load File");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                fileChooser.setTitle("Open Resource File");
                File gameFile = fileChooser.showOpenDialog(primaryStage);
                filePath = gameFile.getAbsolutePath();
                if (gameFile != null){
                    manager = getManager(gameFile);
                }

            }
        });
        Button stop = new Button("Stop");
        stop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (manager != null) {
                    manager.askToStop();
                }
            }
        });
        Button start = new Button("Start");
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (filePath != null) {
                    File file = new File(filePath);
                    VariableHeuristic varHeur = getChosenVariableHeuristic();
                    ValueHeuristic valueHeur = getChosenValueHeurisitic();
                    if (valueHeur == null || varHeur == null) {
                        alert(primaryStage, "Invalid heuristic chosen");
                    } else {
                        manager = getManager(file, varHeur, valueHeur);
                        managerThread = new Thread(manager);
                        managerThread.start();
                    }
                }
            }
        });

        buttons.getChildren().addAll(valueHeuristics, variableHeuristics, models, btn, start, stop);
        root.addRow(1,buttons);

        Scene scene = new Scene(root2, 300, 250);

        primaryStage.setTitle("Nonogram CSP Solver");
        primaryStage.setScene(scene);

        ScrollPane s1 = new ScrollPane();
        s1.setPannable(true);
        s1.setPrefSize(800, 800);
        s1.setContent(root);

        root2.getChildren().add(s1);


        primaryStage.show();
    }

    private void alert(Stage stage,String s) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Error: " + s));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }


    public VariableHeuristic getChosenVariableHeuristic() {
        switch (variableHeuristics.getValue()){
            case DEGREE:
                return new DegreeHeuristic();
            case MRV:
                return new MinimumRemainingValues();
            case LENGTH:
                return new BlockLengthHeuristic();
            case MAX_SUM:
                return new MaxSumVariableHeuristic();
            case MAX_SUM_AND_LENGTH:
                return new BlockLengthAndMaxSumHeuristic();
        }
        return null;
    }

    public ValueHeuristic getChosenValueHeurisitic() {
        switch (valueHeuristics.getValue()){
            case LCV:
                return new LeastConstriningValue();
        }
        return null;
    }

    public Manager getManager(File file) {
        switch (models.getValue()){
            case BLOCK:
                return new BlockManager(file, grid);
            case ROWCOL:
                return new RowColManager(file, grid);
        }
        return null;
    }

    public Manager getManager(File file, VariableHeuristic varHeur, ValueHeuristic valHeur) {
        switch (models.getValue()){
            case BLOCK:
                return new BlockManager(file, grid, varHeur, valHeur, new ArcConsistency());
            case ROWCOL:
                return new RowColManager(file, grid, varHeur, valHeur, new ArcConsistency());
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
