package solver.gui;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import solver.csp.ConstraintHandler;
import solver.csp.Manager;
import solver.csp.NonogramParser;
import solver.csp.handlers.ArcConsistency;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.heuristics.value.ValueHeuristic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class MainWindow extends Application implements UserInterface {
    Counters counters = Counters.getInstance();
    Grid grid;
    TextArea console;
    NonogramParser parser;
    Manager manager;
    String filePath;
    Thread managerThread;
    Pane modelParameters;
    ChoiceBox<Class> valueHeuristics;
    ChoiceBox<Class> variableHeuristics;
    ChoiceBox<Class> models;
    private ChoiceBox<Class> handlers;

    @Override
    public void display(Color[][] colors) {
        grid.setColor(colors);
    }

    @Override
    public void report(String message) {
        console.appendText(message + "\n");
    }

    @Override
    public boolean supportDynamicDisplay() {
        return true;
    }

    private class ClassStringConverter extends StringConverter<Class>{

        @Override
        public String toString(Class cls) {
            return cls.getSimpleName().replaceAll("([A-Z])", " $1");
        }

        @Override
        public Class fromString(String clsName) {
            try {
                return Class.forName(clsName.replace(" ",""));
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        parser = null;
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        grid = new Grid(10, 10);
        console = new TextArea();
        console.setEditable(false);
        Pane gridConsole = new HBox();
        gridConsole.getChildren().addAll(grid, console);
        VBox root2 = new VBox();
        GridPane root = new GridPane();
        root.addRow(1, gridConsole);
        root.setVgap(20);
        final FileChooser fileChooser = new FileChooser();
        final Chooser chooser = new Chooser();
        final ClassStringConverter converter = new ClassStringConverter();

        Pane buttons = new VBox();
        Pane controlButtons = new HBox();



        Button btn = new Button("Load File");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                fileChooser.setTitle("Open Resource File");
                File gameFile = fileChooser.showOpenDialog(primaryStage);
                if(gameFile != null) {
                    filePath = gameFile.getAbsolutePath();
                    primaryStage.setTitle("Nonogram CSP Solver - " + gameFile.getName());
                    try {
                        parser = new NonogramParser(gameFile);
                        grid.setSize(parser.getColAmount(), parser.getRowAmount());
                        grid.setHint(parser.getColumnHints(), parser.getRowHints());
                    } catch (IOException e) {
                        alert(primaryStage, "Couldn't parse file. Is it a valid file?");
                    }
                }
            }
        });

        modelParameters = new HBox();
        models = new ChoiceBox<Class>();
        models.setConverter(converter);
        models.getItems().setAll(chooser.getModels());
        models.valueProperty().addListener(new ChangeListener<Class>() {
            @Override
            public void changed(ObservableValue<? extends Class> observableValue, Class old, Class chosen) {
                valueHeuristics = new ChoiceBox<Class>();
                valueHeuristics.setConverter(converter);
                valueHeuristics.getItems().setAll(chooser.getValueHeuristics(chosen));
                valueHeuristics.setValue(chooser.getDefault(chooser.getValueHeuristics(chosen)));

                variableHeuristics = new ChoiceBox<Class>();
                variableHeuristics.setConverter(converter);
                variableHeuristics.getItems().setAll(chooser.getVariableHeuristics(chosen));
                variableHeuristics.setValue(chooser.getDefault(chooser.getVariableHeuristics(chosen)));

                handlers = new ChoiceBox<Class>();
                handlers.setConverter(converter);
                handlers.getItems().setAll(chooser.getHandlers());
                handlers.setValue(chooser.getDefault(chooser.getHandlers()));
                modelParameters.getChildren().setAll(models, valueHeuristics, variableHeuristics, handlers);
            }
        });
        models.setValue(chooser.getDefault(chooser.getModels()));

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
                counters.reset();
                if (parser != null) {
                    VariableHeuristic varHeur = getChosenVariableHeuristic();
                    ValueHeuristic valueHeur = getChosenValueHeurisitic();
                    ConstraintHandler handler = getChosenContraintHandler();
                    if (valueHeur == null || varHeur == null || handler == null) {
                        alert(primaryStage, "Invalid heuristic or constraint handler chosen");
                    } else {
                        manager = getManager(parser, varHeur, valueHeur, handler);
                        managerThread = new Thread(manager);
                        managerThread.start();
                    }
                } else {
                    alert(primaryStage, "Choose a file first");
                }
            }
        });

        controlButtons.getChildren().addAll(btn, start, stop);
        buttons.getChildren().addAll(new Label("Controls:"), controlButtons, new Label("Parameters:"), modelParameters);
        root.addRow(0,buttons);

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
        Dialogs.showErrorDialog(stage, s, "We had an error:", "Oh No!");
    }


    public VariableHeuristic getChosenVariableHeuristic()  {
        try {
            return (VariableHeuristic) variableHeuristics.getValue().newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public ValueHeuristic getChosenValueHeurisitic() {
        try {
            return (ValueHeuristic) valueHeuristics.getValue().newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }


    private ConstraintHandler getChosenContraintHandler() {
        try {
            return (ConstraintHandler) handlers.getValue().newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }


    public Manager getManager(NonogramParser parser, VariableHeuristic varHeur, ValueHeuristic valHeur, ConstraintHandler handler) {
        Class[] args = new Class[5];
        args[0] = NonogramParser.class;
        args[1] = UserInterface.class;
        args[2] = VariableHeuristic.class;
        args[3] = ValueHeuristic.class;
        args[4] = ConstraintHandler.class;
        try {
            return (Manager) models.getValue().getDeclaredConstructor(args).newInstance(parser, this, varHeur, valHeur, handler);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
