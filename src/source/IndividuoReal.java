package source;

import java.util.ArrayList;

public class IndividuoReal {
    public ArrayList<CamaraReal> camaras;
    public double fitness;

    public IndividuoReal() {
        camaras = new ArrayList<>();
        fitness = 0;
    }

    public IndividuoReal copiar() {
        IndividuoReal copia = new IndividuoReal();
        for(CamaraReal c : camaras) {
            copia.camaras.add(c.copiar());
        }
        copia.fitness = this.fitness;
        return copia;
    }
    
}