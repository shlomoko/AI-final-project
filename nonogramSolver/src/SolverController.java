import gnonograms.app.Controller;
import gnonograms.app.Resource;
import gnonograms.utils.Model;
import gnonograms.utils.My2DCellArray;

import java.awt.*;


/**
 * Created by tmrlvi on 12/01/2016.
 */
public class SolverController extends Controller {
    public SolverController(){
        super();
        setSolving(false);
        // Removing the menu
        view.getContentPane().getComponent(0).setVisible(false);
    }

    /**
     * Updates the labels of the nonogram
     * @param rows the labels of the rows
     * @param cols the labels of the columns
     */
    public void updateLabels(final String[] rows, final String[] cols) {
        EventQueue.invokeLater(new Runnable() { public void run() {
            for (int i = 0; i < rows.length; i++) view.setClueText(i, rows[i], false, false);
            for (int i = 0; i < cols.length; i++) view.setClueText(i, cols[i], true, false);
            view.pack();
        }});
    }

    /**
     * Replaces the models with new one
     * @param newModel the model to draw
     */
    public void drawModel(final Model newModel){
        EventQueue.invokeLater(new Runnable() { public void run() {
            model = newModel;
        }});
    }

    /**
     * Updates the grid. Call when you want to paint
     */
    public void redrawGrid(){
        EventQueue.invokeLater(new Runnable() { public void run() {
            view.redrawGrid();
        }});
    }
}
