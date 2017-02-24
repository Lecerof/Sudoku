import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.InputMismatchException;
import java.util.LinkedList;


public class MySudokuModel implements SudokuModel {

	private int rows=9;
	private int cols=9;
	private int[][] sudoku = new int[rows][cols];
	private MySudokuModel solvedSudoku;
	private int counter = 0;
	
	private LinkedList<Move> moveHistory= new LinkedList<Move>();
	private int moveHistoryIndex = 0; private int moveHistoryBound = 0;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private class Move {
		int row, col, val, oldVal;
		public Move(int r, int c, int v, int oldV) {
			row = r; col = c; val = v; oldVal = oldV;
		}
	}
	private void clearHistory() {
		moveHistoryIndex = 0;
		moveHistoryBound = 0;
		moveHistory = new LinkedList<Move>();
	}
	
	private void addHistory(Move m) {
		if (moveHistoryIndex == moveHistory.size()) {
			moveHistory.add(m);
		}
		else {
			moveHistory.set(moveHistoryIndex, m);

		}
			moveHistoryIndex++;
	}
	public void makeSolvable() {
		moveHistoryBound--;
		while(!isSolvable())
			undo();
			moveHistoryBound--;
	}
	
	public void undo() {
		if (moveHistoryIndex > 0) {
		Move last = moveHistory.get(moveHistoryIndex-1);
		sudoku[last.row][last.col] = last.oldVal;
		moveHistoryIndex--;
		pcs.fireIndexedPropertyChange("undo", (last.row*9+last.col), last.val, last.oldVal);
		}
	}
	public void redo() {
		if (moveHistoryIndex < moveHistoryBound) {
		Move last = moveHistory.get(moveHistoryIndex);
		sudoku[last.row][last.col] = last.val;
		moveHistoryIndex++;
		pcs.fireIndexedPropertyChange("undo", (last.row*9+last.col), last.oldVal, last.val);
		}
	}
	private boolean isSolvable = false;
	
	public MySudokuModel() {
		clear(); // Initiate to 0 explicitly
	}
	
	public MySudokuModel(MySudokuModel s) {
		rows = s.rows;
		cols = s.cols;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				sudoku[i][j] = s.sudoku[i][j];
			}
		}
	}
	
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
		isSolvable = false;
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
	
	private boolean solveHelper(){
		int[] index = this.findLowestSumIndex();
		if ((index[0] == -1 || index[1] == -1)) {
			isSolvable = true;
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
		return isSolvable;
	}
	
	public int[] findLowestSumIndex() { // if no squares left returns -1,-1
		int[] index = {-1,-1}; 	//row, col
		int bestSum = 28;		//all free + 1
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
	
	public boolean isSolvable() {
		MySudokuModel a = new MySudokuModel(this);
		return a.solve();
	}

	
	public boolean isUnique() {
		counter = 0;
		boolean unique = true;
		if (solveHelper()) {
			unique = (counter > 1) ? false : true;
		}
		return unique;
	}
	
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
}
