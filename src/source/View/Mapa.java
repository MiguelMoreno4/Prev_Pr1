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
    public int getCoste(int x, int y) {
        if (esObstaculo(x, y)) return -1; // muro
        int valor = matrizImportancia[y][x]; 
        if (valor == 5)  return 5; // suelo amarillo
        if (valor == 20) return 20; // suelo rojo
        return 1; // suelo blanco
    }

	public int getImportancia(int xOrigen, int yOrigen) {
		// TODO Auto-generated method stub
		return this.matrizImportancia[xOrigen][yOrigen];
	}

	public boolean enRango(int nx, int ny) {
		// TODO Auto-generated method stub
		 return nx >= 0 && nx < columnas && ny >= 0 && ny < filas;
	}
	
	public int getBasePrimeraX() {
	    for (int y = 0; y < filas; y++)
	        for (int x = 0; x < columnas; x++)
	            if (!esObstaculo(x, y)) return x;
	    return 1;
	}

	public int getBasePrimeraY() {
	    for (int y = 0; y < filas; y++)
	        for (int x = 0; x < columnas; x++)
	            if (!esObstaculo(x, y)) return y;
	    return 1;
	}
	
}
