package source.GP;

import source.AST.*;
import java.util.Random;

public class GeneradorAST {
    private static final Random rnd = new Random();

    // Método Ramped half-and-half
    public static NodoBloque generarIndividuo(int profundidadMax, boolean usarFull) {
        NodoBloque raiz = new NodoBloque();
        // Un individuo en la raíz suele tener entre 1 y 3 instrucciones base
        int numInstrucciones = 1 + rnd.nextInt(3);
        for (int i = 0; i < numInstrucciones; i++) {
            raiz.instrucciones.add(generarNodo(0, profundidadMax, usarFull));
        }
        return raiz;
    }

    private static Nodo generarNodo(int profundidadActual, int profundidadMax, boolean usarFull) {
        // Si llegamos al límite de profundidad, o si es "Grow" y toca terminal al azar
        if (profundidadActual >= profundidadMax || (!usarFull && rnd.nextDouble() < 0.5)) {
            return generarTerminal();
        } else {
            // Generar un Nodo IF
            TipoSensor sensor = TipoSensor.values()[rnd.nextInt(TipoSensor.values().length)];
            Operador op = Operador.values()[rnd.nextInt(Operador.values().length)];
            int umbral = 10 + rnd.nextInt(90); // Umbral entre 10 y 100

            NodoCondicional nodoIf = new NodoCondicional(sensor, op, umbral);
            
            // Llenar la rama IF
            nodoIf.ramaIf.instrucciones.add(generarNodo(profundidadActual + 1, profundidadMax, usarFull));
            
            // 50% de probabilidad de tener un bloque ELSE
            if (rnd.nextBoolean()) {
                nodoIf.ramaElse.instrucciones.add(generarNodo(profundidadActual + 1, profundidadMax, usarFull));
            }
            return nodoIf;
        }
    }

    // Regla del PDF: AVANZAR tiene el doble de probabilidad
    public static NodoAccion generarTerminal() {
        int r = rnd.nextInt(4); // 0, 1, 2, 3
        if (r < 2) {
            return new NodoAccion(TipoAccion.AVANZAR); // 50% (Doble probabilidad)
        } else if (r == 2) {
            return new NodoAccion(TipoAccion.GIRAR_IZQ); // 25%
        } else {
            return new NodoAccion(TipoAccion.GIRAR_DER); // 25%
        }
    }
}
