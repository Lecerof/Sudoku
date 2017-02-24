import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

public class MySudokuView extends JPanel implements PropertyChangeListener, KeyListener {
	Square[][] playField = new Square[9][9];
	SudokuModel model;
	SudokuController controller;
	
	public MySudokuView(SudokuModel model, SudokuController controller) {
		this.model = model;
		this.controller = controller;
	    model.addPropertyChangeListener(this);

		
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
	    this.setPreferredSize(new Dimension(500,500));
	    setVisible(true);
	}
	
	
	class Square extends JTextField {
		private int row,col;
		
		public Square() {}
		
		public Square(int r, int c) {
			row = r;
			col = c;
		}
		
		public int getRow() {
			return row;
		}
		public int getCol() {
			return col;
		}
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt instanceof IndexedPropertyChangeEvent) {
			int index = ((IndexedPropertyChangeEvent) evt).getIndex();
			int col = index%9;
			int row = index/9;
			int value = model.getBoard(row,col);
			playField[row][col].setText( (value>0) ? Integer.toString(value) : null );
			
		} else {
			for (int i = 0; i<9; i++) {
				for (int j = 0; j < 9; j++) {
					int val = model.getBoard(i,j);
					Square ref =playField[i][j];
					if (val != 0) {
						ref.setText(Integer.toString(val));
						ref.setEnabled(false);
						ref.setBackground(Color.lightGray);
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
		if (e.isActionKey()) {
			Square a = (Square) KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			moveFromTo(e, a).grabFocus();
		}
	}
	
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
			return s;
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
		} else if (!e.isActionKey()){
			playField[row][col].setText("");
			Toolkit.getDefaultToolkit().beep();
		}
	}

}


	

