package source.GP;

import source.AST.*;
import java.util.Random;

public class GeneradorAST {
    
    public static final Random rnd = new Random();

    // Función principal que refleja tu pseudocódigo
    public static Nodo crearArbolAleatorio(int profundidadActual, int profundidadMax) {
        
        // CASO BASE: Si llegamos al límite, forzamos que sea una acción (Terminal)
        if (profundidadActual >= profundidadMax) {
            return generarTerminal();
        }

        // CASO RECURSIVO: Elegimos aleatoriamente el tipo de nodo (0=Condicional, 1=Bloque, 2=Acción)
        int tipoNodo = rnd.nextInt(3);

        if (tipoNodo == 0) {
            // === ES UN NODO CONDICIONAL ===
            TipoSensor sensor = TipoSensor.values()[rnd.nextInt(TipoSensor.values().length)];
            Operador op = Operador.values()[rnd.nextInt(Operador.values().length)];
            int umbral = 10 + rnd.nextInt(90); // Aleatorio entre 10 y 100

            // Generamos las dos ramas de forma recursiva aumentando la profundidad
            Nodo ramaIf = crearArbolAleatorio(profundidadActual + 1, profundidadMax);
            Nodo ramaElse = crearArbolAleatorio(profundidadActual + 1, profundidadMax);

            return new NodoCondicional(sensor, op, umbral, ramaIf, ramaElse);

        } else if (tipoNodo == 1) {
            // === ES UN NODO BLOQUE ===
            NodoBloque nodoBloque = new NodoBloque();
            int numHijos = 2 + rnd.nextInt(2); // Aleatorio entre 2 y 3 hijos

            for (int i = 0; i < numHijos; i++) {
                nodoBloque.agregarHijo(crearArbolAleatorio(profundidadActual + 1, profundidadMax));
            }
            
            return nodoBloque;

        } else {
            // === ES UN NODO ACCIÓN (HOJA PREMATURA) ===
            // Esto es lo que permite que el árbol no sea siempre 100% simétrico (Método Grow)
            return generarTerminal();
        }
    }

    // Método auxiliar para generar los nodos finales respetando tus reglas originales
    public static Nodo generarTerminal() {
        int r = rnd.nextInt(4); // 0, 1, 2, 3
        if (r < 2) {
            return new NodoAccion(TipoAccion.AVANZAR); // 50% de probabilidad
        } else if (r == 2) {
            return new NodoAccion(TipoAccion.GIRAR_IZQ); // 25% de probabilidad
        } else {
            return new NodoAccion(TipoAccion.GIRAR_DER); // 25% de probabilidad
        }
    }
}