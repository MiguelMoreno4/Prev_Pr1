package source.Escenarios;

import source.View.Mapa;

public class EscenarioDatos {
    public int[][] mapa;
    public int[][] importancia;
    public int numCamaras;
    public int rango;
    public Mapa mapaObj;

    public EscenarioDatos(int[][] mapa, int[][] importancia, int numCamaras, int rango) {
        this.mapa = mapa;
        this.importancia = importancia;
        this.numCamaras = numCamaras;
        this.rango = rango;
        this.mapaObj = new Mapa(mapa.length, mapa[0].length, mapa);
    }
}
