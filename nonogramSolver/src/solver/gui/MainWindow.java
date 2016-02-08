package solver.gui;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import solver.csp.CSPManager;
import solver.csp.heuristics.*;

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
        LENGTH("Longest Block");

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



        Pane buttons = new HBox();
        valueHeuristics = new ChoiceBox<ValueHeuristicsEnum>();
        valueHeuristics.getItems().setAll(ValueHeuristicsEnum.values());
        valueHeuristics.setValue(ValueHeuristicsEnum.LCV);
        variableHeuristics = new ChoiceBox<VariableHeuristicsEnum>();
        variableHeuristics.getItems().setAll(VariableHeuristicsEnum.values());
        variableHeuristics.setValue(VariableHeuristicsEnum.DEGREE);
        Button btn = new Button("Load File");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                fileChooser.setTitle("Open Resource File");
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    VariableHeuristic varHeur = getChosenVariableHeuristic();
                    ValueHeuristic valueHeur = getChosenValueHeurisitic();
                    if (valueHeur == null || varHeur == null) {
                        alert(primaryStage, "Invalid heuristic chosen");
                    } else {
                        manager = new CSPManager(file, grid, varHeur, valueHeur);
                        new Thread(manager).start();

                    }
                }
            }
        });
        Button running = new Button("Running?");
        running.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (manager != null) {
                    alert(primaryStage, "" + manager.getState());
                    if (manager.getState() == Worker.State.FAILED) {
                        manager.exceptionProperty().getValue().printStackTrace();
                    }
                }
            }
        });
        buttons.getChildren().addAll(valueHeuristics, variableHeuristics, btn, running);
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

    public static void main(String[] args) {
        launch(args);
    }
}
