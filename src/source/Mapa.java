package source;

public class Mapa {
    public int filas;
    public int columnas;
    public int[][] obstaculos;

    public Mapa(int filas, int columnas, int[][] obstaculos) {
        this.filas = filas;
        this.columnas = columnas;
        this.obstaculos = obstaculos;
    }

    public boolean esObstaculo(int x, int y) {
        return obstaculos[y][x] == 1;
    }
}
