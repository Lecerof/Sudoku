import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class MySudokuController extends JPanel implements SudokuController, ActionListener {
	JButton undo, redo;
	SudokuModel model;
	JMenuBar menuBar;
	
	
	public MySudokuController(SudokuModel model, JMenuBar menubar) {
		menuBar = menubar;
		menuBar.getComponents();
		
		JMenu fileMenu = new JMenu("File");
		JMenu actionMenu = new JMenu("Action");
		JMenuItem openMenuItem = new JMenuItem();
		JMenuItem clearMenuItem = new JMenuItem("Clear");
		JMenuItem saveMenuItem = new JMenuItem("Save");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		JMenuItem solveMenuItem = new JMenuItem("Solve");
		JMenuItem testMenuItem = new JMenuItem("Test");
		JMenuItem makeSolvableMenuItem = new JMenuItem("Make Solvable");
		JMenuItem generateMenuItem = new JMenuItem("Generate new");
		
		menuBar.add(fileMenu);
		menuBar.add(actionMenu);
		actionMenu.setEnabled(false);

		fileMenu.add(openMenuItem);
		fileMenu.add(generateMenuItem);
		fileMenu.add(clearMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(exitMenuItem);

		actionMenu.add(solveMenuItem);
		actionMenu.add(testMenuItem);
		actionMenu.add(makeSolvableMenuItem);

		saveMenuItem.addActionListener(e -> save());
		generateMenuItem.addActionListener(e -> generate());
		exitMenuItem.addActionListener(e -> System.exit(0));
		solveMenuItem.addActionListener(e -> model.solve());
		testMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(MySudokuController.this,
												"From this point the sudoku has " + 
												((MySudokuModel) model).uniqueSolutions() +
												" solutions"));
		
		makeSolvableMenuItem.addActionListener(e -> ((MySudokuModel) model).makeSolvable());
		
		clearMenuItem.addActionListener(e ->  {
			model.clear();
			setActionEnabled(false);
		});
		
		OpenAction openAction = new OpenAction();
		openMenuItem.setAction(openAction);

		
		undo = new JButton("Undo");
		undo.addActionListener(e -> ((MySudokuModel) model).undo());
		redo = new JButton("Redo");
		redo.addActionListener(e -> ((MySudokuModel) model).redo());
		
		add(undo);
		add(redo);
		
		this.model = model;
	}
	
	private void setActionEnabled(boolean a) {
		MySudokuController.this.menuBar.getMenu(1).setEnabled(a);
	}
	
	
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
	
	private class OpenAction extends AbstractAction {

		public OpenAction() {
			super("New from file");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
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
					     sb.append(System.lineSeparator());
					     line = br.readLine();
					 }
					 br.close();
					 String everything = sb.toString();
					 model.clear();
					 model.setBoard(everything);
					 setActionEnabled(true);
					 if (!model.isSolvable()) {
						 model.clear();
						 setActionEnabled(false);
						 JOptionPane.showMessageDialog(MySudokuController.this, "No solutions");
					 }
					 if (!model.isUnique())
						 JOptionPane.showMessageDialog(MySudokuController.this, "No unique solution");
					 
				} catch (FileNotFoundException ex) {
					JOptionPane.showMessageDialog(MySudokuController.this, "could not open file");
				} catch (IOException exIO) {
					System.out.println("Something went wrong in the filereader");
				} catch (IllegalArgumentException exI) {
					model.clear();
					setActionEnabled(false);
					JOptionPane.showMessageDialog(MySudokuController.this, "Illegal format on  sudoku");
				}
				
			}
			
		
		}
	}
	
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
            	System.out.println("Something went wrong when saving");
            }
        }
	}
	
	private void generate() {}

}
