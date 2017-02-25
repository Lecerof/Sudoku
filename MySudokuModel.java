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
	
	public MySudokuModel(MySudokuModel s) {
		sudoku = cpyArr(s.sudoku);
	}
	
	public void setBoard(int row, int col, int val) {
		if (isLegal(row, col, val)) {
			int oldVal = sudoku[row][col];
			sudoku[row][col] = val;
			addHistory(new Move(row, col, val, oldVal));
			moveHistoryBound = moveHistoryIndex;
			pcs.fireIndexedPropertyChange("setBoard", (row*9+col), oldVal, val);
		} else {
			throw new IllegalArgumentException("Not allowed to put duplicate numbers in rows, columns or block");
		}
	}
	
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
		// fire property change event
	}
	
	/* private methods*/
		
	// extracts a block and puts it in an array of length 9.
	private int[] extractBlock(int row, int col) {
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
	// extracts a column
	private int[] extractCol(int col) {
		int[] tmp = new int[9];
		for(int i = 0; i < 9; i++) {
			tmp[i] = sudoku[i][col];
		}
		return tmp;
	}
	
	// returns true if element is in the list
	private boolean isInArray(int[] b, int a) {	
		for (int i = 0; i < b.length; i++ ) {
			if(b[i] == a) return true;
		}
		return false;		
	}
	
	private int sumEmptyArray(int[] a) {
		int counter = 0;
		for (int e: a) {
			if (e==0) counter++;
		}
		return counter;
	}
	
	private int sumEmpty(int row, int col) {
		int sum = sumEmptyArray(sudoku[row]); 			// count the empty cells in the row
		sum += sumEmptyArray(extractCol(col)); 			// count the empty cells in the column 
		sum += sumEmptyArray(extractBlock(row, col)); 	// count the empty cell in the block
		return sum;
	}

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
