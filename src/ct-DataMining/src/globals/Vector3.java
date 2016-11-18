package globals;

public class Vector3 {
	public double x, y, z;
	
	public Vector3(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX(){return x;}
	public double getY(){return y;}
	public double getZ(){return z;}
	public double getMean(){return (y + x + z)/2d;}
	public double getDelta(){return Math.abs(y - x - z);}
	
}
