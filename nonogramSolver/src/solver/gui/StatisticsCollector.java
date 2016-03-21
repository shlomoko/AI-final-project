package solver.gui;

import com.opencsv.CSVWriter;
import javafx.scene.paint.Color;
import org.apache.commons.io.FileUtils;
import solver.csp.ConstraintHandler;
import solver.csp.Manager;
import solver.csp.NonogramParser;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by tmrlvi on 05/03/2016.
 */
public class StatisticsCollector implements UserInterface {
    Counters counters = Counters.getInstance();
    List<String> header;
    List<String> measureTitles;
    boolean wroteHeaders;
    CSVWriter output;
    Chooser chooser;
    private int iterations;
    Collection<File> files;
    List<List<Class <?>>> possibilities;
    private int timeout;


    public StatisticsCollector(File config, File root) throws IOException {
        chooser = new Chooser();
        files = FileUtils.listFiles(root, new String[]{"gno"}, true);
        if (!config.exists()){
            generatePossibilities(new BufferedWriter(new FileWriter(config)));
        }
        possibilities = readPossibilities(new BufferedReader(new FileReader(config)));
    }

    public void get(CSVWriter output, Integer iterations, int timeout) throws InterruptedException {
        this.timeout = timeout;
        this.iterations = iterations;
        this.output = output;

        wroteHeaders = false;

        header = new LinkedList<String>();
        List<String> row = new ArrayList<String>(5);
        header.add("Game");
        row.add("");
        header.add("Model");
        row.add("");
        header.add("Handler");
        row.add("");
        header.add("Variable Heuristic");
        row.add("");
        header.add("Value Heuristic");
        row.add("");
        measureTitles = new LinkedList<String>();

        for (File game : files) {
            report(game.getName());
            row.set(0, game.getName());
            for (List<Class<?>> possibility : possibilities) {
                runPossibility(game, possibility, row);
            }
        }
    }

    private void runPossibility(File game, List<Class<?>> possibility, List<String> row) throws InterruptedException {
        Class<? extends Manager> managerClass = (Class<? extends Manager>) possibility.get(0);
        Class<? extends ConstraintHandler> handlerClass = (Class<? extends ConstraintHandler>) possibility.get(1);
        Class<? extends VariableHeuristic> varHeurClass = (Class<? extends VariableHeuristic>) possibility.get(2);
        Class<? extends ValueHeuristic> valHeurClass = (Class<? extends ValueHeuristic>) possibility.get(3);
        row.set(1, managerClass.getSimpleName());
        row.set(2, handlerClass.getSimpleName());
        row.set(3, varHeurClass.getSimpleName());
        row.set(4, valHeurClass.getSimpleName());
        System.out.print("Running ");
        for (int i = 0; i < 5; i++){
            System.out.print(row.get(i));
            if (i < 4){
                System.out.print("-");
            }
        }
        System.out.println(" with " + iterations + " iterations!");
        try {
            runAndRecord(new NonogramParser(game), row, valHeurClass, varHeurClass, handlerClass, managerClass);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException | IOException e) {
            e.printStackTrace();
            row.set(5, e.toString());
            for (int i = 6; i < row.size(); i++){
                row.set(i, "");
            }
            writeRow(row);
        }
    }


    private void runAndRecord(NonogramParser game, List<String> row, Class<? extends ValueHeuristic> valHeurClass, Class<? extends  VariableHeuristic> varHeurClass,
                              Class<? extends ConstraintHandler> handlerClass, Class<? extends Manager> managerClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, InterruptedException {

        boolean interrupted = false;
        for (int i = 0; i < iterations; i++) {
            ValueHeuristic valHeur = valHeurClass.newInstance();
            VariableHeuristic varHeur = varHeurClass.newInstance();
            ConstraintHandler handler = handlerClass.newInstance();
            Class[] modelArgs = new Class[5];
            modelArgs[0] = NonogramParser.class;
            modelArgs[1] = UserInterface.class;
            modelArgs[2] = VariableHeuristic.class;
            modelArgs[3] = ValueHeuristic.class;
            modelArgs[4] = ConstraintHandler.class;
            Manager model = managerClass.getConstructor(modelArgs).newInstance(game, this, varHeur, valHeur, handler);

            if (!runWithTimeout(model)){
                interrupted = true;
                break;
            }
        }
        if (!wroteHeaders){
            writeHeader();
            for (String counter : measureTitles) {
                row.add("");
            }
        }

        if (interrupted){
            row.set(5, "TOOK TOO LONG! (" + timeout + " minutes)");
            for (int i = 6; i < row.size(); i++){
                row.set(i, "");
            }
        }
        else {
            int i = 5;
            for (String counter : measureTitles) {
                if (counters.keySetCount().contains(counter)) {
                    row.set(i, "" + counters.getCount(counter) / (double) iterations);
                } else if (counters.keySetTick().contains(counter)) {
                    row.set(i, "" + counters.getAverageTicks(counter));
                } else {
                    row.set(i, "");
                }
                i++;
            }
        }
        writeRow(row);
    }

    private void writeRow(List<String> row){
        output.writeNext(row.toArray(new String[row.size()]));
        try {
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed writing " + row);
        }
        counters.reset();
    }

    private boolean runWithTimeout(final Manager manager) throws InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        final Future<?> f = service.submit(manager);
        try {
            f.get(timeout, TimeUnit.MINUTES);
        } catch ( ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException | TimeoutException e) {
            manager.askToStop();
            service.shutdown();
            service.shutdownNow();
            f.cancel(true);
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            return false;
        }
        return true;
    }

    private void writeHeader() {
        measureTitles.addAll(counters.keySetCount());
        measureTitles.addAll(counters.keySetTick());
        header.addAll(measureTitles);
        output.writeNext(header.toArray(new String[header.size()]));
        wroteHeaders = true;
    }

    @Override
    public void display(Color[][] colors) {
    }

    @Override
    public void report(String message) {
        System.out.println(message);
    }

    @Override
    public boolean supportDynamicDisplay() {
        return false;
    }

    public void  generatePossibilities(BufferedWriter out) throws IOException {
        for (Class<? extends Manager> manager: chooser.getModels()){
            for (Class<?> handler : chooser.getHandlers()){
                for (Class<?> valHeur : chooser.getValueHeuristics(manager)){
                    for (Class<?> varHeur : chooser.getVariableHeuristics(manager)){
                        String option = manager.getSimpleName() + "," + handler.getSimpleName() + "," + varHeur.getSimpleName() + "," + valHeur.getSimpleName();
                        out.write(option);
                        out.newLine();
                    }
                }
            }
        }
        out.close();
    }

    public List<List<Class <?>>> readPossibilities(BufferedReader in) throws IOException {
        List<List<Class <?>>> possibilities = new LinkedList<>();
        String line = in.readLine();
        while (line != null){
            if (!line.startsWith("#")) {
                String[] parts = line.split(",");
                List<Class<?>> possibility = new ArrayList<>();
                possibility.add(chooser.getModel(parts[0]));
                possibility.add(chooser.getHandler(parts[1]));
                possibility.add(chooser.getVariableHeuristic(parts[2], (Class<? extends Manager>) possibility.get(0)));
                possibility.add(chooser.getValueHeuristic(parts[3], (Class<? extends Manager>) possibility.get(0)));
                possibilities.add(possibility);
            }
            line = in.readLine();
        }
        in.close();
        return possibilities;
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        if (args.length != 5){
            System.err.println("Must provide games root directory and output filename and iterations");
            System.err.println("java -cp nonogramSolver.jar solver.hui.StatisticsCollector <running_list> <games_dir> <csv_output> <iterations> <max_minutes>");
            return;
        }
        StatisticsCollector collector = new StatisticsCollector(new File(args[0]), new File(args[1]));
        collector.get(new CSVWriter(new FileWriter(args[2])), Integer.parseInt(args[3]), Integer.parseInt(args[4]));

    }
}
