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

/**
 * Created by tmrlvi on 04/03/2016.
 */
public class Console implements UserInterface {
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
            } else if (args[i].equals("-h")){
                printHelp();
                return;
            }
            else if (fname == null) {
                fname = args[i];
            } else {
                printUsage("Invalid command arguments");
                return;
            }
        }

        // Checking variables and finding relevant classes
        if (fname == null){
            printUsage("Must supply a filename");
            return;
        }
        file =  new File(fname);
        if (!file.isFile() || !file.exists()){
            printUsage("File not found");
            return;
        }


        Class<? extends Manager> modelClass = (Class<? extends Manager>) chooser.getDefault(chooser.getModels());
        if (modelName != null) {
            Class candidate = getClassAsSubclass(modelName, Manager.class);
            if (candidate == null) {
                return;
            }
            modelClass = candidate;
        }

        Class<? extends VariableHeuristic> varHeurClass = (Class<? extends VariableHeuristic>) chooser.getDefault(chooser.getVariableHeuristics(modelClass));
        if (varHeurName != null) {
            Class candidate = getClassAsSubclass(varHeurName, VariableHeuristic.class);
            if (candidate == null) {
                return;
            }
            if (!chooser.getVariableHeuristics(modelClass).contains(candidate)){
                printUsage("The heuristic " + varHeurName + " cannot be used with model " + modelClass.getSimpleName());
                return;
            }
            varHeurClass = candidate;
        }

        Class<? extends ValueHeuristic> valHeurClass = (Class<? extends ValueHeuristic>) chooser.getDefault(chooser.getValueHeuristics(modelClass));
        if (valHeurName != null) {
            Class candidate = getClassAsSubclass(valHeurName, ValueHeuristic.class);
            if (candidate == null) {
                return;
            }
            if (!chooser.getValueHeuristics(modelClass).contains(candidate)){
                printUsage("The heuristic " + valHeurName + " cannot be used with model " + modelClass.getSimpleName());
                return;
            }
            valHeurClass = candidate;
        }

        Class<? extends ConstraintHandler> handlerClass = (Class<? extends ConstraintHandler>) chooser.getDefault(chooser.getHandlers());
        if (modelName != null) {
            Class candidate = getClassAsSubclass(modelName, ConstraintHandler.class);
            if (candidate == null) {
                return;
            }
            handlerClass = candidate;
        }

        // Creating the running environment
        try {
            valHeur = valHeurClass.newInstance();
            varHeur = varHeurClass.newInstance();
            handler = handlerClass.newInstance();
            Class[] modelArgs = new Class[5];
            modelArgs[0] = NonogramParser.class;
            modelArgs[1] = UserInterface.class;
            modelArgs[2] = VariableHeuristic.class;
            modelArgs[3] = ValueHeuristic.class;
            modelArgs[4] = ConstraintHandler.class;
            model = modelClass.getConstructor(modelArgs).newInstance(new NonogramParser(file), this, varHeur, valHeur, handler);
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
        model.run();
    }


    private Class getClassAsSubclass(String name, Class cls){
        Class candidate;
        try {
            candidate = Class.forName(name);
        } catch (ClassNotFoundException e) {
            printUsage("Invalid " + cls.getSimpleName() + ". Type -h to view the list.");
            return null;
        }
        if (!(cls.isAssignableFrom(candidate))){
            printUsage("Invalid "  + cls.getSimpleName() + ". Type -h to view the list.");
            return null;
        }
        return candidate;
    }

    private void printHelp(){
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
        for (int i = 0; i < colors.length; i++){
            for (int j = 0; j < colors[i].length; j++){
                if (colors[i][j] == Color.GREEN) System.out.print("|");
                else if (colors[i][j] == Color.RED) System.out.print("-");
                else if (colors[i][j] == Color.BLACK) System.out.print("+");
                else System.out.print(" ");
            }
            System.out.println();
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
