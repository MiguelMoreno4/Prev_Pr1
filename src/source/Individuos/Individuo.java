package source.Individuos;

import java.util.ArrayList;
import java.util.Random;

import source.Camaras.Camara;
import source.View.Mapa;

public class Individuo {

    public ArrayList<Camara> camaras;
    public double fitness;

    public Individuo() {
        camaras = new ArrayList<>();
        fitness = 0;
    }

    // Copia profunda
    public Individuo copiar() {
        Individuo nuevo = new Individuo();
        for (Camara c : camaras) {
            nuevo.camaras.add(new Camara(c.x, c.y));
        }
        nuevo.fitness = fitness;
        return nuevo;
    }

    // Mutaci�n: mueve una c�mara aleatoriamente
    public void mutar(Mapa mapa) {
    	Random rnd = new Random();
        int idx = rnd.nextInt(camaras.size());

        // mover la c�mara completamente
        int x, y;
        do {
            x = rnd.nextInt(mapa.columnas);
            y = rnd.nextInt(mapa.filas);
        } while (mapa.esObstaculo(x, y));

        camaras.get(idx).x = x;
        camaras.get(idx).y = y;
    }
}