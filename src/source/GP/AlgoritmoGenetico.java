package source.GP;

import source.AST.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlgoritmoGenetico {
    
    private Random rnd = new Random();
    public enum TipoMutacion { SUB_ARBOL, FUNCIONAL, TERMINAL, HOIST, ALEATORIA }

    // Ejecuta una generación completa (Población anterior -> Nueva Población)
    public List<Nodo> evolucionar(List<Nodo> poblacionActual, TipoMutacion tipoMutacion, double probCruce, double probMutacion) {
        List<Nodo> nuevaPoblacion = new ArrayList<>();
        int tamano = poblacionActual.size();

        // 1. Elitismo: Guardamos al mejor directamente
        Nodo mejorGlobal = obtenerMejor(poblacionActual);
        nuevaPoblacion.add(mejorGlobal.clonar());

        // 2. Generar el resto de la población
        while (nuevaPoblacion.size() < tamano) {
            Nodo padre1 = seleccionTorneo(poblacionActual);
            Nodo hijo = padre1.clonar();

            // Cruce
            if (rnd.nextDouble() < probCruce) {
                Nodo padre2 = seleccionTorneo(poblacionActual);
                crucePorSubArbol(hijo, padre2.clonar());
            }

            // Mutación
            if (rnd.nextDouble() < probMutacion) {
                hijo = aplicarMutacion(hijo, tipoMutacion);
            }

            nuevaPoblacion.add(hijo);
        }
        return nuevaPoblacion;
    }

    // --- SELECCIÓN ---
    private Nodo seleccionTorneo(List<Nodo> poblacion) {
        Nodo mejor = poblacion.get(rnd.nextInt(poblacion.size()));
        double mejorFitness = Evaluador.evaluarIndividuo(mejor);

        for (int i = 0; i < 2; i++) { // Torneo de 3
            Nodo contendiente = poblacion.get(rnd.nextInt(poblacion.size()));
            if (Evaluador.evaluarIndividuo(contendiente) > mejorFitness) {
                mejor = contendiente;
                mejorFitness = Evaluador.evaluarIndividuo(contendiente);
            }
        }
        return mejor;
    }

    private Nodo obtenerMejor(List<Nodo> poblacion) {
        Nodo mejor = poblacion.get(0);
        double mejorF = Evaluador.evaluarIndividuo(mejor);
        for (Nodo ind : poblacion) {
            double f = Evaluador.evaluarIndividuo(ind);
            if (f > mejorF) { 
                mejor = ind; 
                mejorF = f; 
            }
        }
        return mejor;
    }

    // --- CRUCE (Intercambio de ramas anidadas) ---
    private void crucePorSubArbol(Nodo hijo1, Nodo hijo2) {
        List<NodoBloque> bloques1 = obtenerTodosLosBloques(hijo1);
        List<NodoBloque> bloques2 = obtenerTodosLosBloques(hijo2);

        if (bloques1.isEmpty() || bloques2.isEmpty()) return;

        NodoBloque b1 = bloques1.get(rnd.nextInt(bloques1.size()));
        NodoBloque b2 = bloques2.get(rnd.nextInt(bloques2.size()));

        if (!b1.hijos.isEmpty() && !b2.hijos.isEmpty()) {
            int idx1 = rnd.nextInt(b1.hijos.size());
            int idx2 = rnd.nextInt(b2.hijos.size());
            
            // Intercambiamos los nodos
            Nodo temp = b1.hijos.get(idx1);
            b1.hijos.set(idx1, b2.hijos.get(idx2).clonar());
            b2.hijos.set(idx2, temp.clonar());
        }
    }

    // --- MUTACIONES ---
    private Nodo aplicarMutacion(Nodo hijo, TipoMutacion tipo) {
        if (tipo == TipoMutacion.ALEATORIA) {
            tipo = TipoMutacion.values()[rnd.nextInt(4)]; // Elige entre las 4 primeras
        }

        switch (tipo) {
            case HOIST: // Poda: Asciende una sub-rama interna para que sustituya a la raíz
                List<NodoBloque> bloques = obtenerTodosLosBloques(hijo);
                if (bloques.size() > 1) {
                    // Coge un bloque interno al azar y lo convierte en el nuevo individuo completo
                    return bloques.get(1 + rnd.nextInt(bloques.size() - 1)).clonar();
                }
                break;

            case SUB_ARBOL: // Reemplaza un nodo aleatorio por una rama nueva
                List<NodoBloque> blq = obtenerTodosLosBloques(hijo);
                if (!blq.isEmpty()) {
                    NodoBloque b = blq.get(rnd.nextInt(blq.size()));
                    if (!b.hijos.isEmpty()) {
                        int idx = rnd.nextInt(b.hijos.size());
                        // Usa la nueva clase GeneradorAST para crear una sub-rama de prof. máxima 3
                        b.hijos.set(idx, GeneradorAST.crearArbolAleatorio(0, 3));
                    }
                }
                break;

            case FUNCIONAL: // Altera operador, sensor o valor numérico en un IF
                List<NodoCondicional> condicionales = obtenerTodosLosIF(hijo);
                if (!condicionales.isEmpty()) {
                    NodoCondicional c = condicionales.get(rnd.nextInt(condicionales.size()));
                    int r = rnd.nextInt(3);
                    if (r == 0) c.sensor = TipoSensor.values()[rnd.nextInt(TipoSensor.values().length)];
                    else if (r == 1) c.operador = Operador.values()[rnd.nextInt(Operador.values().length)];
                    else c.umbral = 10 + rnd.nextInt(90);
                }
                break;

            case TERMINAL: // Altera un terminal (Avanzar / Girar) sin modificar estructura
                List<NodoAccion> terminales = obtenerTodasLasAcciones(hijo);
                if (!terminales.isEmpty()) {
                    NodoAccion a = terminales.get(rnd.nextInt(terminales.size()));
                    a.accion = TipoAccion.values()[rnd.nextInt(TipoAccion.values().length)];
                }
                break;
        }
        return hijo;
    }

    // --- MÉTODOS AUXILIARES DE BÚSQUEDA EN EL ÁRBOL (RECURSIVOS) ---
    private List<NodoBloque> obtenerTodosLosBloques(Nodo n) {
        List<NodoBloque> lista = new ArrayList<>();
        if (n instanceof NodoBloque) {
            NodoBloque bloque = (NodoBloque) n;
            lista.add(bloque);
            for (Nodo hijo : bloque.hijos) lista.addAll(obtenerTodosLosBloques(hijo));
            
        } else if (n instanceof NodoCondicional) {
            NodoCondicional cond = (NodoCondicional) n;
            if (cond.ramaIf != null) lista.addAll(obtenerTodosLosBloques(cond.ramaIf));
            if (cond.ramaElse != null) lista.addAll(obtenerTodosLosBloques(cond.ramaElse));
        }
        return lista;
    }

    private List<NodoCondicional> obtenerTodosLosIF(Nodo n) {
        List<NodoCondicional> lista = new ArrayList<>();
        if (n instanceof NodoCondicional) {
            NodoCondicional cond = (NodoCondicional) n;
            lista.add(cond);
            if (cond.ramaIf != null) lista.addAll(obtenerTodosLosIF(cond.ramaIf));
            if (cond.ramaElse != null) lista.addAll(obtenerTodosLosIF(cond.ramaElse));
            
        } else if (n instanceof NodoBloque) {
            for (Nodo hijo : ((NodoBloque) n).hijos) lista.addAll(obtenerTodosLosIF(hijo));
        }
        return lista;
    }

    private List<NodoAccion> obtenerTodasLasAcciones(Nodo n) {
        List<NodoAccion> lista = new ArrayList<>();
        if (n instanceof NodoAccion) {
            lista.add((NodoAccion) n);
            
        } else if (n instanceof NodoCondicional) {
            NodoCondicional cond = (NodoCondicional) n;
            if (cond.ramaIf != null) lista.addAll(obtenerTodasLasAcciones(cond.ramaIf));
            if (cond.ramaElse != null) lista.addAll(obtenerTodasLasAcciones(cond.ramaElse));
            
        } else if (n instanceof NodoBloque) {
            for (Nodo hijo : ((NodoBloque) n).hijos) lista.addAll(obtenerTodasLasAcciones(hijo));
        }
        return lista;
    }
}