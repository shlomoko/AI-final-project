package solver.gui;

import javafx.scene.paint.Color;

/**
 * Created by tmrlvi on 04/03/2016.
 */
public interface UserInterface {
    void display(Color[][] colors);
    void report(String message);
    boolean supportDynamicDisplay();
}
