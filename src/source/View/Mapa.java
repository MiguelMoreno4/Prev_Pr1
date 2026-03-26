package source.View;

import java.util.Random;

public class Mapa {
    // El enunciado exige estrictamente 15x15
    public static final int FILAS = 15;
    public static final int COLUMNAS = 15;

    // Sustituimos las matrices de números por un enumerado claro
    public enum TipoCasilla {
        SUELO,    // Gris oscuro (Libre)
        MURO,     // Rojo oscuro (Obstáculo)
        ARENA,    // Naranja (Penaliza)
        MUESTRA   // Amarillo (Objetivo principal)
    }

    public TipoCasilla[][] casillas;

    // Ya no hay "Escenarios", solo le pasamos una semilla (ej: 3000)
    public Mapa(long semilla) {
        casillas = new TipoCasilla[COLUMNAS][FILAS];
        generarMapa(semilla);
    }

    private void generarMapa(long semilla) {
        // La clave de la práctica: Random con semilla
        Random rnd = new Random(semilla); 

        // 1. Rellenar todo de SUELO inicialmente
        for (int x = 0; x < COLUMNAS; x++) {
            for (int y = 0; y < FILAS; y++) {
                casillas[x][y] = TipoCasilla.SUELO;
            }
        }

        // 2. Poner Muros infranqueables en los bordes exteriores
        for (int i = 0; i < COLUMNAS; i++) {
            casillas[i][0] = TipoCasilla.MURO;
            casillas[i][FILAS - 1] = TipoCasilla.MURO;
            casillas[0][i] = TipoCasilla.MURO;
            casillas[COLUMNAS - 1][i] = TipoCasilla.MURO;
        }

        // 3. Despejar la salida: El Rover SIEMPRE sale de (1,1) según el PDF
        casillas[1][1] = TipoCasilla.SUELO;

        // 4. Colocar obstáculos al azar (si usamos la semilla 3000, siempre caerán en el mismo sitio)
        colocarElementos(rnd, TipoCasilla.MURO, 15);     // 15 muros internos
        colocarElementos(rnd, TipoCasilla.ARENA, 20);    // 20 bancos de arena
        colocarElementos(rnd, TipoCasilla.MUESTRA, 10);  // 10 muestras científicas
    }

    private void colocarElementos(Random rnd, TipoCasilla tipo, int cantidad) {
        int colocados = 0;
        while (colocados < cantidad) {
            int x = rnd.nextInt(COLUMNAS);
            int y = rnd.nextInt(FILAS);
            
            // Solo lo colocamos si es suelo y no tapamos la salida (1,1)
            if (casillas[x][y] == TipoCasilla.SUELO && !(x == 1 && y == 1)) {
                casillas[x][y] = tipo;
                colocados++;
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