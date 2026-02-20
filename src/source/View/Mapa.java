package source.View;

public class Mapa {
    public int filas;
    public int columnas;
    public int[][] obstaculos;
    public int[][] matrizImportancia;

    public Mapa(int filas, int columnas, int[][] obstaculos) {
        this.filas = filas;
        this.columnas = columnas;
        this.obstaculos = obstaculos;
        this.matrizImportancia = new int[filas][columnas];
    }

    public boolean esObstaculo(int x, int y) {
        return obstaculos[y][x] == 1;
    }

    public void setMatrizImportancia(int[][] imp) {
        this.matrizImportancia = imp;
    }

    public int getValorImportancia(int x, int y) {
        if (matrizImportancia == null) return 1; // Si no hay matriz, valor base
        if (y >= 0 && y < matrizImportancia.length && x >= 0 && x < matrizImportancia[0].length) {
            return matrizImportancia[y][x];
        }
        return 1;
    }
}
