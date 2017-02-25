import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * Class MySudokuController
 * deals with the communication between the view and the model
 * 
 * @author Jonas Lecerof
 *
 */
public class MySudokuController extends JPanel
				implements SudokuController,ActionListener {

	private static final long serialVersionUID = 1L; // It complains otherwise
	JButton undo, redo;
	SudokuModel model;
	JMenuBar menuBar;
	
	/**
	 * Constructor for creating a new MySudokuController object
	 * @param model reference to the model that you want to use your
	 * controller on
	 * @param menubar reference to a JMenuBar that you want to attach
	 * the JMenuItems from the constructor to
	 */
	public MySudokuController(SudokuModel model, JMenuBar menubar) {
		menuBar = menubar;
		this.model = model;
		
		// create all buttons/menuitems
		undo = new JButton("Undo");
		redo = new JButton("Redo");
		
		JMenu fileMenu = new JMenu("File");
		JMenu actionMenu = new JMenu("Action");
		
		JMenuItem openMenuItem = new JMenuItem("New/Open");
		JMenuItem clearMenuItem = new JMenuItem("Clear");
		JMenuItem saveMenuItem = new JMenuItem("Save");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		JMenuItem solveMenuItem = new JMenuItem("Solve");
		JMenuItem testMenuItem = new JMenuItem("Test");
		JMenuItem makeSolvableMenuItem = new JMenuItem("Make Solvable");
		JMenuItem generateMenuItem = new JMenuItem("Generate new");
		
		// Add all the buttons to respective component
		add(undo);
		add(redo);
		
		menuBar.add(fileMenu);
		menuBar.add(actionMenu);
		actionMenu.setEnabled(false); // nothing to solve if nothing is loaded

		fileMenu.add(openMenuItem);
		fileMenu.add(generateMenuItem);
		fileMenu.add(clearMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(exitMenuItem);

		actionMenu.add(solveMenuItem);
		actionMenu.add(testMenuItem);
		actionMenu.add(makeSolvableMenuItem);

		// Add all actionlisteners to the buttons/menus
		undo.addActionListener(e -> ((MySudokuModel) model).undo());
		redo.addActionListener(e -> ((MySudokuModel) model).redo());
		
		openMenuItem.addActionListener(e -> openFromFile());
		generateMenuItem.addActionListener(e -> generate());
		saveMenuItem.addActionListener(e -> save());
		exitMenuItem.addActionListener(e -> System.exit(0));
		
		solveMenuItem.addActionListener(e -> model.solve());
		testMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(MySudokuController.this,
												"From this point the sudoku has " + 
												((MySudokuModel) model).uniqueSolutions() +
												" solutions"));
		makeSolvableMenuItem.addActionListener(e -> ((MySudokuModel) model).makeSolvable());
		clearMenuItem.addActionListener(e ->  {	model.clear();
												setActionEnabled(false);
												});

		
		
		
		
		
	}
	
	/**
	 * setActionEnabled
	 * function for setting the second item in the menu bar as enabled or disabled
	 * @param a true is enabled, false is disabled
	 */
	private void setActionEnabled(boolean a) {
		MySudokuController.this.menuBar.getMenu(1).setEnabled(a);
	}
	
	/**
	 * input
	 * function for putting a number from the view into the model.
	 * @param row the row you want to set in the model
	 * @param col the column you want to set int the model
	 * @param value the value you want to set in the model.
	 * @return boolean value that is true if setBoard was successful
	 * and false if an exception was thrown by parseInt or setBoard.
	 */
	@Override
	public boolean input(int row, int col, char value) {
		try {
			model.setBoard(row, col, Integer.parseInt(Character.toString(value)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {}
	
	
	/**
	 * openFromFile
	 * function for dealing with opening a sudoku from a file.
	 * Uses a JFileChooser and generates an appropriate message
	 * if something goes wrong in either trying to open a file
	 * or if something is wrong with the sudoku in the file.
	 * 
	 * Illegal sudokus will not be set in the model, but sudokus
	 * with multiple solutions will. Clears the board and the model
	 * if something goes wrong.
	 */
	private void openFromFile() {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			int res = fc.showOpenDialog(null);
			if (res == JFileChooser.APPROVE_OPTION) {
				String filename = fc.getSelectedFile().getAbsolutePath();
				try {
					 BufferedReader br = new BufferedReader(new FileReader(filename));
					 StringBuilder sb = new StringBuilder();
					 String line = br.readLine();

					 while (line != null) {
					     sb.append(line);
					     sb.append(System.lineSeparator()); // Comment: If there is a better
					     line = br.readLine();				// way to read from a file, please let
					 }										// me know.
					 br.close();
					 String everything = sb.toString();
					 model.clear();
					 model.setBoard(everything);
					 setActionEnabled(true);
					 if (!model.isSolvable()) {
						 model.clear();
						 setActionEnabled(false);
						 JOptionPane.showMessageDialog(MySudokuController.this,
								 						"No solutions");
					 }
					 if (!model.isUnique())
						 JOptionPane.showMessageDialog(MySudokuController.this,
								 						"No unique solution");
					 
				} catch (FileNotFoundException ex) {
					JOptionPane.showMessageDialog(MySudokuController.this,
														"could not open file");
				} catch (IOException exIO) {
					System.out.println("Something went wrong in the filereader");
				} catch (IllegalArgumentException exI) {
					model.clear();
					setActionEnabled(false);
					JOptionPane.showMessageDialog(MySudokuController.this,
														"Illegal format on  sudoku");
				}
				
			}
			
		
		}
	
	/**
	 * save
	 * function for saving a sudoku. Before it saves it makes the puzzle
	 * solvable so it will revert the model until its stage before a wrongly
	 * chosen input was made
	 */
	private void save() {
		JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
		int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
            FileWriter filewriter = new FileWriter(file);
            ((MySudokuModel)model).makeSolvable();
            filewriter.write(model.getBoard());
            filewriter.close();
            } catch (IOException e1) {
            	JOptionPane.showMessageDialog(MySudokuController.this,
            						"Something went wrong when saving");
            }
        }
	}
	
	private void generate() {
		JOptionPane.showMessageDialog(MySudokuController.this,
				"TODO, sorry");
	}

}
