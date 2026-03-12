package source.Camaras;

public class CamaraReal {
    public double x;
    public double y;
    public double theta; // grados [0,360)

    public CamaraReal(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta % 360;
        if(this.theta < 0) this.theta += 360;
    }

    public CamaraReal copiar() {
        return new CamaraReal(x, y, theta);
    }
    public int getX() {
    	return (int) x;
    }
    public int getY() {
    	return (int) y;
    }
}