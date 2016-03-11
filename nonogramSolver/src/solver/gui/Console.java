package solver.gui;

import javafx.scene.paint.Color;
import solver.csp.ConstraintHandler;
import solver.csp.Manager;
import solver.csp.NonogramParser;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.block.BlockManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tmrlvi on 04/03/2016.
 */
public class Console implements UserInterface {
    private int iterations;
    Counters counters = Counters.getInstance(); //Holding the item so it will always be referenced
    File file;
    Chooser chooser;
    ValueHeuristic valHeur;
    VariableHeuristic varHeur;
    ConstraintHandler handler;
    Manager model;

    public Console(String []args) {
        chooser = new Chooser();
        String fname = null, modelName = null, varHeurName = null, valHeurName = null,
                handlerName = null;
        iterations = 1;
        // Parsing command line
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                i++;
                modelName = args[i];
            } else if (args[i].equals("-l")) {
                i++;
                valHeurName = args[i];
            } else if (args[i].equals("-r")) {
                i++;
                varHeurName = args[i];
            } else if (args[i].equals("-c")) {
                i++;
                handlerName = args[i];
            } else if (args[i].equals("-h")) {
                printHelp();
                return;
            } else if (args[i].equals("-i")) {
                i++;
                iterations = Integer.parseInt(args[i]);
            } else if (fname == null) {
                fname = args[i];
            } else {
                printUsage("Invalid command arguments");
                return;
            }
        }

        // Checking variables and finding relevant classes
        if (fname == null) {
            printUsage("Must supply a filename");
            return;
        }
        file = new File(fname);
        if (!file.isFile() || !file.exists()) {
            printUsage("File not found");
            return;
        }


        Class<? extends Manager> modelClass = (Class<? extends Manager>) chooser.getDefault(chooser.getModels());
        if (modelName != null) {
            Class candidate = chooser.getModel(modelName);
            if (candidate == null) {
                printUsage("Invalid manager. Type -h to view the list.");
                return;
            }
            modelClass = candidate;
        }

        Class<? extends VariableHeuristic> varHeurClass = (Class<? extends VariableHeuristic>) chooser.getDefault(chooser.getVariableHeuristics(modelClass));
        if (varHeurName != null) {
            Class candidate = chooser.getVariableHeuristic(varHeurName, modelClass);
            if (candidate == null) {
                printUsage("Invalid variable heuristic. Type -h to view the list.");
                return;
            }
            varHeurClass = candidate;
        }

        Class<? extends ValueHeuristic> valHeurClass = (Class<? extends ValueHeuristic>) chooser.getDefault(chooser.getValueHeuristics(modelClass));
        if (valHeurName != null) {
            Class candidate = chooser.getValueHeuristic(valHeurName, modelClass);
            if (candidate == null) {
                printUsage("Invalid value heuristic. Type -h to view the list.");
                return;
            }
            valHeurClass = candidate;
        }

        Class<? extends ConstraintHandler> handlerClass = (Class<? extends ConstraintHandler>) chooser.getDefault(chooser.getHandlers());
        if (handlerName != null) {
            Class candidate = chooser.getHandler(handlerName);
            if (candidate == null) {
                printUsage("Invalid handler. Type -h to view the list.");
                return;
            }
            handlerClass = candidate;
        }

        // Creating the running environment
        try {
            Class[] modelArgs = new Class[5];
            modelArgs[0] = NonogramParser.class;
            modelArgs[1] = UserInterface.class;
            modelArgs[2] = VariableHeuristic.class;
            modelArgs[3] = ValueHeuristic.class;
            modelArgs[4] = ConstraintHandler.class;

            for (int i = 0; i < iterations; i++) {
                valHeur = valHeurClass.newInstance();
                varHeur = varHeurClass.newInstance();
                handler = handlerClass.newInstance();
                model = modelClass.getConstructor(modelArgs).newInstance(new NonogramParser(file), this, varHeur, valHeur, handler);
                model.run();
            }
            report("===============================");
            report("==Total Counters==");
            for (String key : counters.keySetCount()) {
                report(key + ": " + ((double) counters.getCount(key)) / iterations + " times.");
            }
            for (String key : counters.keySetTick()) {
                report(key + ": " + counters.getAverageTicks(key) + " miliseconds on average,");
            }
        } catch (NoSuchMethodException e) {
            printUsage("Sorry. Had runtime error. Please try again.");
        } catch (InstantiationException e) {
            printUsage("Sorry. Had runtime error. Please try again.");
        } catch (IllegalAccessException e) {
            printUsage("Sorry. Had runtime error. Please try again.");
        } catch (InvocationTargetException e) {
            printUsage("Sorry. Had runtime error. Please try again.");
        } catch (IOException e) {
            printUsage("Failed parsing file. Check filename and rerun.");
        }
    }

    private void printHelp(){
        report("Models:");
        Set<Class<?>> var = new HashSet<>();
        Set<Class<?>> val = new HashSet<>();
        for (Class<? extends Manager> model : chooser.getModels()){
            report("    " + model.getSimpleName());
            var.addAll(chooser.getVariableHeuristics(model));
            val.addAll(chooser.getValueHeuristics(model));
        }
        report("VarHeur:");
        for (Class<?> v : var){
            report("    " + v.getSimpleName());
        }
        report("ValueHeur:");
        for (Class<?> v : val){
            report("    " + v.getSimpleName());
        }
        
        printUsage("");
    }

    private void printUsage(String message){
        System.err.println(message);
        System.err.println("Usage: nonogramSolver [-m <manager>] [-l <valueHeuristic>] [-r <variableHeuristic>] [-c <constraintHandler>] [-h] <fname>" );
    }


    public static void main(String[] args){
        new Console(args);
    }

    @Override
    public void display(Color[][] colors) {
        if (colors.length > 0) {
            for (int j = 0; j < colors[0].length; j++) {
                for (int i = 0; i < colors.length; i++) {
                    if (colors[i][j] == Color.GREEN) System.out.print("| ");
                    else if (colors[i][j] == Color.RED) System.out.print("- ");
                    else if (colors[i][j] == Color.BLACK) System.out.print("+ ");
                    else System.out.print("  ");
                }
                System.out.println();
            }
        }
    }

    @Override
    public void report(String message) {
        System.out.println(message);
    }

    @Override
    public boolean supportDynamicDisplay() {
        return false;
    }
}
