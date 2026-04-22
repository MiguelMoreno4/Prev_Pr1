package source.View;

import java.util.Random;

public class Mapa {
    
    public static final int FILAS = 15;
    public static final int COLUMNAS = 15;

    
    public enum TipoCasilla {
        SUELO,    // Gris oscuro (Libre)
        MURO,     // Rojo oscuro (Obstáculo)
        ARENA,    // Naranja (Penaliza)
        MUESTRA   // Amarillo (Objetivo principal)
    }

    public TipoCasilla[][] casillas;

    
    public Mapa(long semilla) {
        casillas = new TipoCasilla[COLUMNAS][FILAS];
        generarMapa(semilla);
    }

    private void generarMapa(long semilla) {
        Random rand = new Random(semilla); 

        for (int y = 0; y < FILAS; y++) {       // i = y (Alto)
            for (int x = 0; x < COLUMNAS; x++) { // j = x (Ancho)
                
               
                if (y == 0 || y == FILAS - 1 || x == 0 || x == COLUMNAS - 1) {
                    casillas[x][y] = TipoCasilla.MURO;//Muros bordes
                } 
                
                else if (rand.nextDouble() < 0.15 && (y != 1 || x != 1)) {
                    casillas[x][y] = TipoCasilla.MURO;     // Paredes
                } 
                else if (rand.nextDouble() < 0.15 && (y != 1 || x != 1)) {
                    casillas[x][y] = TipoCasilla.MUESTRA;    // Arena
                } 
                else if (rand.nextDouble() < 0.08 && (y != 1 || x != 1)) {
                    casillas[x][y] = TipoCasilla.ARENA;  // Muestras
                } 
                else {
                    // Si no cae en los porcentajes, o si es la casilla (1,1), queda libre
                    casillas[x][y] = TipoCasilla.SUELO; 
                }
            }
        }
    }
  

    // --- MÉTODOS ÚTILES PARA EL ROVER ---

    public boolean enRango(int x, int y) {
        return x >= 0 && x < COLUMNAS && y >= 0 && y < FILAS;
    }

    // El sensor del Rover usará esto para "mirar" qué hay delante
    public TipoCasilla getCasilla(int x, int y) {
        if (!enRango(x, y)) return TipoCasilla.MURO;
        return casillas[x][y];
    }

    // Cuando el Rover pase por encima de una muestra, llamaremos a esto
    public void recogerMuestra(int x, int y) {
        if (enRango(x, y) && casillas[x][y] == TipoCasilla.MUESTRA) {
            casillas[x][y] = TipoCasilla.SUELO; // La muestra desaparece
        }
    }
    public int getFilas() {return FILAS;}
    public int getCol() {return this.COLUMNAS;}
}