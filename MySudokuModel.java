import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;


public class MySudokuModel implements SudokuModel {
	
	private int rows=9;
	private int cols=9;
	private int[][] sudoku = new int[rows][cols];
	private MySudokuModel solvedSudoku;
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
			moveHistoryBound = moveHistoryIndex;
			pcs.fireIndexedPropertyChange("setBoard", (row*9+col), oldVal, val);
		} else {
			throw new IllegalArgumentException("Not allowed to put duplicate"
										+ "numbers in rows, columns or block");
		}
	}
	
	/**
	 * 
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
		clearHistory();
		pcs.firePropertyChange("setBoardStr", oldsud, sudoku);
	}
	
	public int getBoard(int row, int col) {
		return sudoku[row][col];
	}
	
	/**
	 * 
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
	 * 
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
	 * 
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
		if (solveHelper()) {
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
	private boolean solveHelper(){
		int[] index = this.findLowestSumIndex();
		if ((index[0] == -1 || index[1] == -1)) {
			counter++;
			solvedSudoku = new MySudokuModel(this);			
		}
		else {
		for(int i=1; i<10;i++) {
			if(isLegal(index[0], index[1], i)) {
				this.sudoku[index[0]][index[1]] = i;
				this.solveHelper();
				this.sudoku[index[0]][index[1]] = 0;
			}
		} }
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
		if (solveHelper()) {
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
		solveHelper();
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
		sudoku[last.row][last.col] = last.oldVal;
		moveHistoryIndex--;
		pcs.fireIndexedPropertyChange("undo", (last.row*9+last.col), last.val, last.oldVal);
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
		int rows = m.length; int cols = m[0].length;
		int[][] res = new int[rows][cols];
		for (int i = 0; i< rows; i++) {
			for(int j = 0; j<cols; j++) {
				res[i][j] = m[i][j];
			}
		}
		return res;
	}
}
