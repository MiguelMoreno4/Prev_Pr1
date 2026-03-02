package source.Camaras;

public class Camara {
    public int x;
    public int y;

    public Camara(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
