package source.AST;

import java.util.ArrayList;
import java.util.List;
import source.View.Mapa;
import source.View.Rover;

public class NodoBloque implements Nodo {
    public List<Nodo> instrucciones;

    public NodoBloque() {
        this.instrucciones = new ArrayList<>();
    }

    @Override
    public boolean ejecutar(Rover rover, Mapa mapa) {
        for (Nodo hijo : instrucciones) {
            // Si el hijo ejecuta una acción física, detenemos el bloque (1 acción por tick)
            if (hijo.ejecutar(rover, mapa)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Nodo clonar() {
        NodoBloque copia = new NodoBloque();
        for (Nodo hijo : instrucciones) {
            copia.instrucciones.add(hijo.clonar());
        }
        return copia;
    }

    @Override
    public String imprimir(String tab) {
        StringBuilder sb = new StringBuilder();
        for (Nodo hijo : instrucciones) {
            sb.append(hijo.imprimir(tab));
        }
        return sb.toString();
    }

    @Override
    public int contarNodos() {
        int total = 1; // Este bloque cuenta como 1 nodo
        for (Nodo hijo : instrucciones) {
            total += hijo.contarNodos();
        }
        return total;
    }
}