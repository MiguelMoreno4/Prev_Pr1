package source.Individuos;

import java.util.*;

public class IndividuoMTSP {

    public List<Integer> cromosoma;
    public double fitness;

    public IndividuoMTSP(List<Integer> cromosoma) {
        this.cromosoma = new ArrayList<>(cromosoma);
    }

    public IndividuoMTSP copiar() {
        IndividuoMTSP copia = new IndividuoMTSP(this.cromosoma);
        copia.fitness = this.fitness;
        return copia;
    }
}
