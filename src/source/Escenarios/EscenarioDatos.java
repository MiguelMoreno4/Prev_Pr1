package source.Escenarios;

import source.View.Mapa;

public class EscenarioDatos {
    public int[][] mapa;
    public int[][] importancia;
    public int numCamaras;
    public int rango;
    public Mapa mapaObj;
    public double apertura;
    public EscenarioDatos(int[][] mapa, int[][] importancia, int numCamaras, int rango, double apertura) {
        this.mapa = mapa;
        this.importancia = importancia;
        this.numCamaras = numCamaras;
        this.rango = rango;
        this.apertura=apertura;
        this.mapaObj = new Mapa(mapa.length, mapa[0].length, mapa);
    }
}
