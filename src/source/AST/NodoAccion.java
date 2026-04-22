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
        // 1. Ejecutamos la acción física. 
        switch (accion) {
            case AVANZAR: 
                rover.avanzar(mapa); 
                break;
            case GIRAR_IZQ: 
                rover.girarIzq(); 
                break;
            case GIRAR_DER: 
                rover.girarDer(); 
                break;
        }

        // 2. Control de seguridad por si el Rover se queda en negativo
        if (rover.bateria < 0) {
            rover.bateria = 0;
        }

        // 3. Devolvemos TRUE para bloquear más acciones físicas en este tick
        return true; 
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