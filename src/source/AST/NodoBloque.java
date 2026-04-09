package source.AST;

import source.View.Mapa;
import source.View.Rover;
import java.util.ArrayList;
import java.util.List;

public class NodoBloque implements Nodo {
    
    public List<Nodo> hijos;

    public NodoBloque() {
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(Nodo hijo) {
        this.hijos.add(hijo);
    }

    @Override
    public boolean ejecutar(Rover rover, Mapa mapa) {
        for (Nodo hijo : hijos) {
            // Si un hijo ejecuta una acción física, detenemos el bloque (1 acción por tick)
            if (hijo.ejecutar(rover, mapa)) {
                return true; 
            }
        }
        return false;
    }

    @Override
    public Nodo clonar() {
        NodoBloque copia = new NodoBloque();
        for (Nodo hijo : this.hijos) {
            copia.hijos.add(hijo.clonar()); // Clona recursivamente cada hijo del bloque
        }
        return copia;
    }
    @Override
    public String imprimir(String tab) {
        StringBuilder sb = new StringBuilder();
        for (Nodo hijo : hijos) {
            sb.append(hijo.imprimir(tab));
        }
        return sb.toString();
    }

    @Override
    public int contarNodos() {
        int total = 1; 
        for (Nodo hijo : hijos) {
            total += hijo.contarNodos();
        }
        return total;
    }
}