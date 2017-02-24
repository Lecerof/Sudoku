import java.awt.*;
import javax.swing.*;

public class SudokuMain extends JFrame {

  public SudokuMain() {
	JMenuBar menuBar = new JMenuBar();
    SudokuModel model       = new MySudokuModel();
    MySudokuController ctrl = new MySudokuController(model, menuBar);
    MySudokuView view       = new MySudokuView(model, ctrl);
    add(menuBar, BorderLayout.NORTH);
    add(view, BorderLayout.CENTER);
    add(ctrl, BorderLayout.SOUTH); 
    setSize(420,420);
    setLocationRelativeTo(null); // centrera
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public static void main(String[] arg) {
    new SudokuMain();
  } 
}