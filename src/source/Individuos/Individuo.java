package source.Individuos;

import java.util.ArrayList;
import java.util.List;
import source.Camaras.Camara;

public class Individuo {

    // Para representación binaria
    public boolean[] cromosoma;

    // Para visualización y compatibilidad con tu GUI
    public List<Camara> camaras;

    public double fitness;

    // Constructor binario
    public Individuo(int longitudCromosoma) {
        this.cromosoma = new boolean[longitudCromosoma];
        this.camaras = new ArrayList<>();
    }

    // Constructor vacío (para visualización)
    public Individuo() {
        this.camaras = new ArrayList<>();
    }

    public Individuo copiar() {
        Individuo copia;

        if (this.cromosoma != null) {
            copia = new Individuo(this.cromosoma.length);
            for (int i = 0; i < cromosoma.length; i++) {
                copia.cromosoma[i] = this.cromosoma[i];
            }
        } else {
            copia = new Individuo();
        }

        copia.fitness = this.fitness;
        copia.camaras = new ArrayList<>(this.camaras);

        return copia;
    }
}