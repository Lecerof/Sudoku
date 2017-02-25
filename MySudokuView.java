import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

public class MySudokuView extends JPanel implements PropertyChangeListener, KeyListener {
	Square[][] playField = new Square[9][9];
	SudokuModel model;
	SudokuController controller;
	
	/**
	 * constructor for MySudokuView
	 * @param model reference to the model
	 * @param controller
	 * sets the view of the model. Numbers added to the model from a file
	 * will be disabled from input and shown as gray.
	 */
	public MySudokuView(SudokuModel model, SudokuController controller) {
		this.model = model;
		this.controller = controller;
	    model.addPropertyChangeListener(this);
		
	    /*Initiates the playfield, wich basically is the view of the sudoku puzzle*/
		setLayout(new GridLayout(3, 3));
		for (int k = 0; k <9; k++) {
			JPanel block = new JPanel();
			block.setLayout(new GridLayout(3,3));
			block.setBorder( new LineBorder(Color.BLACK));
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int row = i+3*(k/3);
					int col = j+3*(k%3);
					Square s = new Square(row, col);
					s.addKeyListener(this);
					s.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
					s.setDisabledTextColor(Color.BLACK);
					playField[row][col] = s;
					s.setHorizontalAlignment(JTextField.CENTER);
					block.add(s);
					
				}
			}
			this.add(block);
		}
	    setVisible(true);
	}
	
	/**
	 * class Square
	 * Helperclass for making the playfield. Extends JTextfield
	 * with two instance variables for wich row and column the
	 * field represents
	 */
	private class Square extends JTextField {
		private int row,col;
		
		public Square() {}
		
		/**
		 * constructor for making a square
		 * @param r what row it belongs to
		 * @param c what column it belongs to
		 */
		public Square(int r, int c) {
			row = r;
			col = c;
		}
		
		/**
		 * getRow returns the instance variable row of the Square
		 * @return row the row the Square belongs to
		 */
		public int getRow() {
			return row;
		}
		/**
		 * getCol returns the instance variable col of the Square
		 * @return col the column the Square belongs to
		 */
		public int getCol() {
			return col;
		}
		
	}

	@Override
	/**
	 * propertyChange deals with what happens if the model was changed by something.
	 * @param evt the PropertyChangeEvent may be of indexed type or of nonindexed type.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt instanceof IndexedPropertyChangeEvent) {
			// What happends if it is of indexed type.
			int index = ((IndexedPropertyChangeEvent) evt).getIndex();
			int col = index%9;
			int row = index/9;
			int value = model.getBoard(row,col);
			/* if the value is greater than 0 it will set the Square to the value. Else
			   it will set the Square to null. */
			playField[row][col].setText( (value>0) ? Integer.toString(value) : null );
			
		} else {
			// what happends if it is of nonindexed type, ie. clear() and setBoard(String a)
			for (int i = 0; i<9; i++) {
				for (int j = 0; j < 9; j++) {
					int val = model.getBoard(i,j);
					Square ref =playField[i][j];
					if (val != 0) {
						ref.setText(Integer.toString(val));
						ref.setEnabled(false);
						ref.setBackground(new Color(224, 224, 224));
						ref.setBorder(BorderFactory.createRaisedBevelBorder());
					} else {
						ref.setText(null);
						ref.setEnabled(true);
						ref.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
						ref.setBackground(Color.white);
						
					}
				}
			}
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		// For moving the active view from one editable square to another
		if (e.isActionKey()) {
			Square a = (Square) KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			moveFromTo(e, a).grabFocus();
		}
	}
	
	/**
	 * moveFromTo 
	 * @param e the KeyEvent, takes care of UP, DOWN, LEFT, RIGHT
	 * @param s the current Square you want to move from
	 * @return Square the first Square in the chosen direction that you are able to move to
	 * from your current location
	 */
	private Square moveFromTo(KeyEvent e, Square s) {
		int row = s.row; int col = s.col;
		do {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP: row--;
				break;
			case KeyEvent.VK_DOWN: row++;
				break;
			case KeyEvent.VK_LEFT: col--;
				break;
			case KeyEvent.VK_RIGHT: col++;
				break;
			default: break;
			}
		} while(row >= 0 && col >= 0 &&
				  row < 9 && col < 9 &&
				  !playField[row][col].isEnabled());
		if(row >= 9 || row < 0 || col >= 9 || col < 0)
			return s; /* was not able to find any possible Square in that 
						direction to move to */
		else
			return  playField[row][col];
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Square a  = (Square) e.getSource();
		int row = a.getRow(); int col = a.getCol();
		if (controller.input(row, col, e.getKeyChar())) {
			int value = model.getBoard(row,col);
			playField[row][col].setText( (value>0) ? Integer.toString(value) : null );
		} else if (!e.isActionKey()){ // actionKeys does not count as input
			playField[row][col].setText("");
			Toolkit.getDefaultToolkit().beep();
		}
	}

}


	

