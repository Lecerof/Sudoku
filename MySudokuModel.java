import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Class MySudokuModel
 * makes up the model of the sudoku
 * @author Jonas Lecerof
 *
 */
public class MySudokuModel implements SudokuModel {
	
	private int rows=9;
	private int cols=9;
	private int[][] sudoku = new int[rows][cols];
	private MySudokuModel solvedSudoku;
	//private boolean isSolved = false;
	private int counter = 0;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	//  Instance variables related to the history function
	private LinkedList<Move> moveHistory= new LinkedList<Move>();
	private int moveHistoryIndex = 0;
	private int moveHistoryBound = 0;	
	
	public MySudokuModel() {
		clear(); // Initiate to 0 explicitly
	}
	
	/**
	 * constructor for copying a MySudokuModel object
	 * @param s the MySudokuModel to be copied
	 */
	public MySudokuModel(MySudokuModel s) {
		sudoku = cpyArr(s.sudoku);
	}
	
	/**
	 * setboard
	 * sets the sudoku on a specific index. It also adds it to the
	 * history queue. Throws an exception if one tries to add an illegal
	 * number to the sudoku, ie. a duplication of a number already existing
	 * in a row, column or block
	 * @param row the row index
	 * @param col the column index
	 * @param val value to be set
	 * @throws IllegalArgumentException if one tries to put duplicate numbers
	 * in the sudoku
	 */
	public void setBoard(int row, int col, int val) {
		if (isLegal(row, col, val)) {
			int oldVal = sudoku[row][col];
			sudoku[row][col] = val;
			addHistory(new Move(row, col, val, oldVal));
			moveHistoryBound = moveHistoryIndex; // this means that a value was added by user
			pcs.fireIndexedPropertyChange("setBoard", (row*9+col), oldVal, val);
		} else {
			throw new IllegalArgumentException("Not allowed to put duplicate"
										+ "numbers in rows, columns or block");
		}
	}
	
	/**
	 * setBoard
	 * sets the sudoku from a string. If it is not a valid sudoku
	 * it throws an IllegalArgumentException
	 * @param input the string that are to be parsed to a sudoku
	 * @throws IllegalArgumentException if the sudoku is not of
	 * valid format
	 */
	public void setBoard(String input) {
		int[][] oldsud = cpyArr(sudoku);
		String[] parts = input.split("\n");
		
		for(int i = 0; i<rows; i++) {
			for(int j = 0; j<cols; j++){
				try {
					int token = Integer.parseInt(parts[i].substring(j,j+1));
					setBoard(i, j, token);
				} catch (NumberFormatException e) {
					setBoard(i, j, 0);
				} catch (IndexOutOfBoundsException e) {
					throw new IllegalArgumentException("Illegal format");
				}
			}
		}
		clearHistory(); // since i use setboard i need to clear the history
		pcs.firePropertyChange("setBoardStr", oldsud, sudoku);
	}
	
	/**
	 * getboard
	 * gets the value of the square with given index as long as the
	 * indexes are valid. Otherwise it returns 0
	 * @param row rowindex of the value
	 * @param col columnindex of the value
	 * @return value of the square or 0 depending on if the indexes
	 * are valid indexes for a sudoku puzzle
	 */
	public int getBoard(int row, int col) {
		int value = (row < 0 || row >= rows || col < 0 || col >= cols) ?
													0 : sudoku[row][col];
		return value;
	}
	
	/**
	 * getBoard
	 * converts a board into a string where each row is ended by
	 *  the newline sign
	 *  @return string with a sudokuboard
	 */
	public String getBoard() {
		String s = "";
		for(int[] row : sudoku) {
			for (int col : row) {
				s += col;
			}
			s += "\n";
		}
		return s;
	
	}
	
	
	/**
	 * isLegal
	 * determines if a value is a legal value to put in the
	 * sudoku. A value is legal if it is 0, the same value
	 * as before or there is no occurance of it in a row
	 * column or block.
	 * @param row the row index of the value to be checked
	 * @param col the column index of the value to be checked
	 * @param val the integer value to be checked
	 * @return boolean true if it is a legal option
	 */
	public boolean isLegal(int row, int col, int val) {
		if (val == 0 || val == sudoku[row][col]) {
			return true;
		} else {
			int[] block = extractBlock(row, col);
			if (!isInArray(block, val) && 
				!isInArray(sudoku[row], val) && 
				!isInArray(extractCol(col), val)) {
				return true;
			}
			return false;
		}	
	}
	
	/**
	 * clear
	 * clears the sudoku and the history and fires a propertychange
	 */
	public void clear() {
		int[][] oldsud = new int[rows][cols];
		counter = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				oldsud[i][j] = sudoku[i][j];
				sudoku[i][j] = 0;
			}
		}
		clearHistory();
		pcs.firePropertyChange("clear", oldsud, sudoku);;
	}
	
	/**
	 * extractBlock
	 * extracts a block from the sudoku and puts it in an array
	 * if row or col is outside the sudoku boundaries it returns null
	 * @param row the row index of the square contained by one block
	 * @param col the column index of the square contained by one block
	 * @return array with all the numbers contained by the block
	 */
	private int[] extractBlock(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols)
			return null;
		int blockY = row/3;
		int blockX = col/3;
		int[] tmp = new int[9];
		for ( int i = 0 ; i < 3 ; i++ ) {
			for ( int j=0; j <3; j++) {
				tmp[i*3 + j] = sudoku[blockY*3 + i][blockX*3+j];
			}
		}
		return tmp;
	}
	
	/**
	 * extractCol
	 * extracts the entire column out of a sudoku and puts it in an array
	 * if user tries to extract a column outside of the sudoku it returns
	 * null instead
	 * @param col the column to be extracted.
	 * @return column as an array
	 */
	private int[] extractCol(int col) {
		if (col < 0 || col >= cols) return null;
		int[] tmp = new int[rows];		
		for(int i = 0; i < 9; i++) {
			tmp[i] = sudoku[i][col];
		}
		return tmp;
	}
	
	/**
	 * isInArray
	 * checks if a value is in an array
	 * @param b the array to be checked
	 * @param a the value you want to look for
	 * @return boolean true if the element is in the array
	 */
	private boolean isInArray(int[] b, int a) {	
		for (int i = 0; i < b.length; i++ ) {
			if(b[i] == a) return true;
		}
		return false;		
	}
	
	/**
	 * sumEmptyArray
	 * counts all the zeros in an array
	 * @param a
	 * @return counted value
	 */
	private int sumEmptyArray(int[] a) {
		int counter = 0;
		for (int e: a) {
			if (e==0) counter++;
		}
		return counter;
	}
	
	/**
	 * sumEmpty
	 * sums the empy indexes in each row, column and block of the solution
	 * @param row the row index
	 * @param col the column index
	 * @return sum of the amount of empty indexes
	 */
	private int sumEmpty(int row, int col) {
		int sum = sumEmptyArray(sudoku[row]); 			// count the empty cells in the row
		sum += sumEmptyArray(extractCol(col)); 			// count the empty cells in the column 
		sum += sumEmptyArray(extractBlock(row, col)); 	// count the empty cell in the block
		return sum;
	}
	
	/**
	 * solve
	 * tries to solve a sudoku by using solveHelper. if there is a solution
	 * it will set the sudoku to that solution and return true and fire
	 * a propertychange.
	 * @return boolean true if there is a solution
	 */
	public boolean solve() {
		counter = 0;
		if (solveHelper(1)) {
			int[][] oldsud = cpyArr(sudoku);
			sudoku = solvedSudoku.sudoku;
			pcs.firePropertyChange("setBoardStr", oldsud, sudoku);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * solveHelper
	 * uses backtracking to generate solutions. If there are more
	 * than one solution it will find them aswell and increase the
	 * counter.
	 * @return boolean true if it manage to find a solution
	 */
	private boolean solveHelper(int counterLimit){
		int[] index = this.findLowestSumIndex();
		if ((index[0] == -1 || index[1] == -1)) {
			counter++;
			solvedSudoku = new MySudokuModel(this);			
		}
		else {
			for(int i=1; i<10;i++) {
				if(isLegal(index[0], index[1], i) && (counter <= counterLimit)) {
					this.sudoku[index[0]][index[1]] = i;
					this.solveHelper(counterLimit);
					this.sudoku[index[0]][index[1]] = 0;
				}
			}
		}
		return counter > 0;
	}
	
	/**
	 * findLowestSumIndex
	 * finds the index with the least amounts of free squares
	 * in the row, column and block.
	 * @return index an array with two elements: [row, column]
	 */
	private int[] findLowestSumIndex() {
		int[] index = {-1,-1}; 	//row, col
		int bestSum = 28;		//all free
		for (int i = 0; i<9; i++ ) {
			for (int j = 0; j<9; j++) {
				if (this.sudoku[i][j] == 0) {
					int sum = this.sumEmpty(i,j);
					if (sum<bestSum) {
						bestSum = sum;
						index[0] =i;
						index[1] =j;
					}
				}
			}
		}
		return index;
	}
	
	/**
	 * isSolvable
	 * copies the model and solves the copy
	 * @return boolean true if there is a solutions to the current
	 * state of the model
	 */
	public boolean isSolvable() {
		MySudokuModel a = new MySudokuModel(this);
		return a.solve();
	}

	/**
	 * isUnique
	 * calculates if the sudoku has a unique solution
	 * @return boolean true if it has a unique solution
	 */
	public boolean isUnique() {
		counter = 0;
		boolean unique = true;
		if (solveHelper(2)) {
			unique = (counter > 1) ? false : true;
		}
		return unique;
	}
	
	/**
	 * uniqueSolutions
	 * calculates the amount of unique solutions the
	 * current status of the sudoku puzzle has
	 * @return counter, an integer value of the number of solutions
	 */
	public int uniqueSolutions() {
		counter = 0;
		solveHelper(10);
		return counter;
	}
	
	@Override
	public String toString() {
		return getBoard();
	}

	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);		
	}

	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);		
	}
	
	/**
	 * class Move
	 * related to the history queue
	 * stores the previous value, the indexes and the new value of
	 * an value in the sudoku puzzle.
	 */
	private class Move {
		int row, col, val, oldVal;
		public Move(int r, int c, int v, int oldV) {
			row = r; col = c; val = v; oldVal = oldV;
		}
	}
	
	/**
	 * clearHistory
	 * clears the history queue and variables connected to it
	 */
	private void clearHistory() {
		moveHistoryIndex = 0;
		moveHistoryBound = 0;
		moveHistory = new LinkedList<Move>();
	}
	
	/**
	 * 
	 * @param m Move object that is to be put in the queue. If the
	 * index is equal to the size, then you will add it to the list.
	 * if it is not then it will write over whatever was in the list
	 * at that index.
	 */
	private void addHistory(Move m) {
		if (moveHistoryIndex == moveHistory.size()) {
			moveHistory.add(m);
		}
		else 
			moveHistory.set(moveHistoryIndex, m);
		moveHistoryIndex++;
	}
	
	/**
	 * makeSolvable
	 * removes all inputs made after and including the one that is not part
	 * of at least one solution
	 */
	public void makeSolvable() {
		moveHistoryBound--;
		while(!isSolvable())
			undo();
			moveHistoryBound--;
	}
	
	/**
	 * undo
	 * changes the sudoku instance variable to the stage before the current input
	 * in the history queue
	 */
	public void undo() {
		if (moveHistoryIndex > 0) {
		Move last = moveHistory.get(moveHistoryIndex-1);
		sudoku[last.row][last.col] = last.oldVal; //no need to use setboard
		moveHistoryIndex--;						  //since all values are safe
		pcs.fireIndexedPropertyChange("undo", (last.row*9+last.col),
												last.val, last.oldVal);
		}
	}
	
	/**
	 * redo
	 * reverts whatever undo did
	 */
	public void redo() {
		if (moveHistoryIndex < moveHistoryBound) {
		Move last = moveHistory.get(moveHistoryIndex);
		sudoku[last.row][last.col] = last.val;
		moveHistoryIndex++;
		pcs.fireIndexedPropertyChange("redo", (last.row*9+last.col), last.oldVal, last.val);
		}
	}
	
	/**
	 * cpyArr is a method for copying an m by n matrix
	 * @param m an m by n matrix
	 * @return reference to the copied matrix
	 */
	private int[][] cpyArr (int[][] m) {
		int[][] res = new int[m.length][];
		for (int i = 0; i<m.length; i++) {
			res[i] = Arrays.copyOf(m[i], m[i].length);
		}
		return res;
	}
	
	public void removeWrong() {
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++) {
				if (sudoku[i][j] != solvedSudoku.sudoku[i][j])
					setBoard(i, j, 0);
			}
		}
	}
	
	public void generate() {
		clear();
		int counter = 0;
		while(counter<10) {
			try {
			int row = (int) (Math.random()*9);
			int col = (int) (Math.random()*9);
			int value = (int) (Math.random()*9+1);
			setBoard(row, col, value);
			counter++;
			} catch (Exception e) {}
		}
		
		int[] range = randperm(IntStream.iterate(0, n -> n + 1).limit(81).toArray());
		for (int e : range) {
			int tmp = sudoku[e/9][e%9];
			sudoku[e/9][e%9] = 0;
			if (!isUnique()){
				sudoku[e/9][e%9] = tmp;
			}
			
		}
		setBoard(this.getBoard());
	}
	
	private int[] randperm(int[] a) {
		int index, temp;
		Random random = new Random();
	    for (int i = a.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	        temp = a[index];
	        a[index] = a[i];
	        a[i] = temp;
	    }
	    return a;
	}
}
