
public interface SudokuModel extends Model {
	  void clear();
	  void setBoard(int row, int col, int val); 
	  void setBoard(String input); 
	  int  getBoard(int row, int col);
	  String getBoard();
	  boolean isLegal(int row, int col, int val);
	  boolean solve(); 
	  boolean isSolvable();
	  boolean isUnique();
}
