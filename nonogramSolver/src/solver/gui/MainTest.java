package solver.gui;

import gnonograms.utils.Model;

public class MainTest {
    public static void main(String[] args) throws InterruptedException {

        SolverController c = new SolverController();
        c.resize(10,10);
        while (true) {
            int[] k = {1, 1, 1, 1, 1, 1, 1,1,1,1};
            for (int j=0; j<10; j++) {

                k[j] = 2;
                Model model = new Model();
                model.setDimensions(10,10);
                for (int i = 0; i < 10; i++) model.setRowDataFromArray(i, k);
                c.drawModel(model);
                Thread.sleep(1);
                k[j] = 1;
                c.redrawGrid();
            }
        }
    }
}