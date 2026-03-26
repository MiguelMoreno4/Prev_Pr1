package source.AST;

import source.View.Mapa;
import source.View.Rover;

public class NodoAccion implements Nodo {
    public TipoAccion accion;

    public NodoAccion(TipoAccion accion) {
        this.accion = accion;
    }

    @Override
    public boolean ejecutar(Rover rover, Mapa mapa) {
        switch (accion) {
            case AVANZAR: rover.avanzar(mapa); break;
            case GIRAR_IZQ: rover.girarIzq(); break;
            case GIRAR_DER: rover.girarDer(); break;
        }
        return true; // Se ejecutó una acción física, fin del tick
    }

    @Override
    public Nodo clonar() {
        return new NodoAccion(this.accion);
    }

    @Override
    public String imprimir(String tab) {
        return tab + accion.toString() + "();\n";
    }

    @Override
    public int contarNodos() {
        return 1;
    }
}