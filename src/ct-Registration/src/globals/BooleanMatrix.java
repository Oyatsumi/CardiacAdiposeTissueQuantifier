package globals;

public class BooleanMatrix {
	private boolean[][] m;
	private int counter = 0;

	public BooleanMatrix(int width, int height){
		m = new boolean[height][width];
	}
	
	public void check(int x, int y){
		if (x >= 0 && y >= 0 && x < m[0].length && y < m.length){
			m[y][x] = true;
			counter ++;
		}
	}
	
	public boolean isChecked(int x, int y){
		if (x < 0 || y < 0 || x >= m[0].length || y >= m.length)
			return false;
		return m[y][x];
	}
	
	
	public int getChecked(){return this.counter;}
}
