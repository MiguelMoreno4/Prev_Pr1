package source.GP;

import source.AST.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneradorAST {
    
    public static final Random rnd = new Random();

    // =========================================================
    // 1. INICIALIZACIÓN RAMPED HALF-AND-HALF (NUEVO)
    // =========================================================
    public static List<Nodo> inicializarPoblacion(int tamanoPob, int profMin, int profMax) {
        List<Nodo> poblacion = new ArrayList<>();
        
        int niveles = profMax - profMin + 1;
        int individuosPorNivel = tamanoPob / niveles;
        int creados = 0;

        for (int prof = profMin; prof <= profMax; prof++) {
            // Ajuste por si la división no es exacta
            int limiteNivel = (prof == profMax) ? (tamanoPob - creados) : individuosPorNivel;
            
            for (int i = 0; i < limiteNivel; i++) {
                Nodo nuevoIndividuo;
                if (i < limiteNivel / 2) {
                    nuevoIndividuo = generarArbolFull(0, prof);
                } else {
                    nuevoIndividuo = generarArbolGrow(0, prof);
                }
                poblacion.add(nuevoIndividuo);
                creados++;
            }
        }
        return poblacion;
    }

    // =========================================================
    // 2. MÉTODO FULL (Solo escoge Funciones hasta llegar al final)
    // =========================================================
    private static Nodo generarArbolFull(int profActual, int profMax) {
        // CASO BASE: Límite de profundidad
        if (profActual >= profMax) {
            return generarTerminal(); // TUS REGLAS DE PROBABILIDAD (50/25/25)
        }

        // Obligatorio Nodo con hijos (Condicional o Bloque)
        boolean elegirBloque = rnd.nextBoolean();
        
        if (elegirBloque) {
            NodoBloque nodoBloque = new NodoBloque(); // TU CÓDIGO
            int numHijos = 2 + rnd.nextInt(2); 
            for (int i = 0; i < numHijos; i++) {
                nodoBloque.agregarHijo(generarArbolFull(profActual + 1, profMax));
            }
            return nodoBloque;
        } else {
            return crearCondicionalBase(generarArbolFull(profActual + 1, profMax), 
                                        generarArbolFull(profActual + 1, profMax));
        }
    }

    // =========================================================
    // 3. MÉTODO GROW (Crecimiento libre, puede ser hoja prematura)
    // =========================================================
    private static Nodo generarArbolGrow(int profActual, int profMax) {
        // CASO BASE OBLIGATORIO
        if (profActual >= profMax) {
            return generarTerminal();
        }

        // LIBERTAD: Exactamente la misma lógica que tenías en tu código original
        int tipoNodo = rnd.nextInt(3);

        if (tipoNodo == 0) {
            return crearCondicionalBase(generarArbolGrow(profActual + 1, profMax), 
                                        generarArbolGrow(profActual + 1, profMax));
        } else if (tipoNodo == 1) {
            NodoBloque nodoBloque = new NodoBloque();
            int numHijos = 2 + rnd.nextInt(2);
            for (int i = 0; i < numHijos; i++) {
                nodoBloque.agregarHijo(generarArbolGrow(profActual + 1, profMax));
            }
            return nodoBloque;
        } else {
            // Hoja prematura (Grow)
            return generarTerminal(); 
        }
    }

    // =========================================================
    // 4. MÉTODOS AUXILIARES Y COMPATIBILIDAD CON TU CÓDIGO
    // =========================================================
    
    // Mantenemos tu método original intacto por si las Mutaciones lo usan
    public static Nodo crearArbolAleatorio(int profundidadActual, int profundidadMax) {
        // Tu método original es matemáticamente idéntico a un crecimiento tipo "Grow"
        return generarArbolGrow(profundidadActual, profundidadMax);
    }

    // TU CÓDIGO ORIGINAL: Respetando las probabilidades 50%, 25%, 25%
    public static Nodo generarTerminal() {
        int r = rnd.nextInt(4); // 0, 1, 2, 3
        if (r < 2) {
            return new NodoAccion(TipoAccion.AVANZAR); 
        } else if (r == 2) {
            return new NodoAccion(TipoAccion.GIRAR_IZQ); 
        } else {
            return new NodoAccion(TipoAccion.GIRAR_DER); 
        }
    }

    // TU CÓDIGO ORIGINAL de inicialización de condicionales (Extraído a método)
    private static NodoCondicional crearCondicionalBase(Nodo ramaIf, Nodo ramaElse) {
        TipoSensor sensor = TipoSensor.values()[rnd.nextInt(TipoSensor.values().length)];
        Operador op = Operador.values()[rnd.nextInt(Operador.values().length)];
        int umbral = 10 + rnd.nextInt(90); // Aleatorio entre 10 y 100
        
        return new NodoCondicional(sensor, op, umbral, ramaIf, ramaElse);
    }
}