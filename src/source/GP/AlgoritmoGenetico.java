package source.GP;

import source.AST.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlgoritmoGenetico {
    
    private Random rnd = new Random();
    public enum TipoMutacion { SUB_ARBOL, FUNCIONAL, TERMINAL, HOIST, ALEATORIA }

    // Ejecuta una generación completa (Población anterior -> Nueva Población)
    public List<Nodo> evolucionar(List<Nodo> poblacionActual, TipoMutacion tipoMutacion, double probCruce, double probMutacion, double numElite) {
        List<Nodo> nuevaPoblacion = new ArrayList<>();
        int tamano = poblacionActual.size();

        // 🌟 OPTIMIZACIÓN: Calculamos el fitness de todos UNA SOLA VEZ
        Map<Nodo, Double> fitnessCache = new HashMap<>();
        for (Nodo ind : poblacionActual) {
            fitnessCache.put(ind, Evaluador.evaluarIndividuo(ind));
        }

        // 1. Elitismo: Ordenamos usando el caché y guardamos la cantidad elegida
        int cantidadElites = (int) numElite; // Convertimos tu double a int por si viene de un JSpinner
        List<Nodo> poblacionOrdenada = new ArrayList<>(poblacionActual);
        poblacionOrdenada.sort((a, b) -> Double.compare(fitnessCache.get(b), fitnessCache.get(a)));

        for (int i = 0; i < cantidadElites && i < tamano; i++) {
            nuevaPoblacion.add(poblacionOrdenada.get(i).clonar());
        }

        // 2. Generar el resto (Tu código intacto)
        while (nuevaPoblacion.size() < tamano) {
            // Pasamos el caché a los torneos
            Nodo padre1 = seleccionTorneo(poblacionActual, fitnessCache);
            Nodo hijo = padre1.clonar(); 

            if (rnd.nextDouble() < probCruce) {
                Nodo padre2 = seleccionTorneo(poblacionActual, fitnessCache);
                hijo = crucePorSubArbol(hijo, padre2); 
            }

            if (rnd.nextDouble() < probMutacion) {
                hijo = aplicarMutacion(hijo, tipoMutacion);
            }

            nuevaPoblacion.add(hijo);
        }
        
        return nuevaPoblacion;
    }

    // Fíjate en cómo los métodos auxiliares ahora leen del Map de forma instantánea:

    private Nodo seleccionTorneo(List<Nodo> poblacion, Map<Nodo, Double> cache) {
        Nodo mejor = poblacion.get(rnd.nextInt(poblacion.size()));
        double mejorFitness = cache.get(mejor); // <--- LECTURA INSTANTÁNEA

        for (int i = 0; i < 2; i++) { 
            Nodo contendiente = poblacion.get(rnd.nextInt(poblacion.size()));
            double fitContendiente = cache.get(contendiente); // <--- LECTURA INSTANTÁNEA
            
            if (fitContendiente > mejorFitness) {
                mejor = contendiente;
                mejorFitness = fitContendiente;
            }
        }
        return mejor;
    }

    private Nodo obtenerMejor(List<Nodo> poblacion, Map<Nodo, Double> cache) {
        Nodo mejor = poblacion.get(0);
        double mejorF = cache.get(mejor);
        
        for (Nodo ind : poblacion) {
            double f = cache.get(ind);
            if (f > mejorF) { 
                mejor = ind; 
                mejorF = f; 
            }
        }
        return mejor;
    }

   

    // =========================================================
    // CRUCE (Intercambio de sub-árboles avanzado)
    // =========================================================
    private Nodo crucePorSubArbol(Nodo hijo1, Nodo padre2) {
        // Seleccionamos un punto de cruce al azar en ambos árboles
        Nodo nodoCruce1 = seleccionarNodoAlAzar(hijo1);
        Nodo nodoCruce2 = seleccionarNodoAlAzar(padre2);

        // Sustituimos el nodo en el hijo1 por una copia profunda de la rama del padre2
        return sustituirNodo(hijo1, nodoCruce1, nodoCruce2.clonar());
    }

    private Nodo seleccionarNodoAlAzar(Nodo raiz) {
        List<Nodo> todos = new ArrayList<>();
        recolectarNodos(raiz, todos);
        return todos.get(rnd.nextInt(todos.size()));
    }

    private void recolectarNodos(Nodo actual, List<Nodo> lista) {
        if (actual == null) return;
        lista.add(actual);

        if (actual instanceof NodoBloque) {
            for (Nodo hijo : ((NodoBloque) actual).hijos) recolectarNodos(hijo, lista);
        } else if (actual instanceof NodoCondicional) {
            NodoCondicional nc = (NodoCondicional) actual;
            recolectarNodos(nc.ramaIf, lista);
            recolectarNodos(nc.ramaElse, lista);
        }
    }

    private Nodo sustituirNodo(Nodo raizActual, Nodo viejo, Nodo nuevo) {
        if (raizActual == viejo) return nuevo;

        if (raizActual instanceof NodoBloque) {
            NodoBloque nb = (NodoBloque) raizActual;
            for (int i = 0; i < nb.hijos.size(); i++) {
                if (nb.hijos.get(i) == viejo) {
                    nb.hijos.set(i, nuevo);
                    return raizActual;
                } else {
                    sustituirNodo(nb.hijos.get(i), viejo, nuevo);
                }
            }
        } else if (raizActual instanceof NodoCondicional) {
            NodoCondicional nc = (NodoCondicional) raizActual;
            
            if (nc.ramaIf == viejo) nc.ramaIf = nuevo;
            else if (nc.ramaIf != null) sustituirNodo(nc.ramaIf, viejo, nuevo);

            if (nc.ramaElse == viejo) nc.ramaElse = nuevo;
            else if (nc.ramaElse != null) sustituirNodo(nc.ramaElse, viejo, nuevo);
        }
        return raizActual;
    }

    // =========================================================
    // MUTACIONES (Tus reglas originales)
    // =========================================================
    private Nodo aplicarMutacion(Nodo hijo, TipoMutacion tipo) {
        if (tipo == TipoMutacion.ALEATORIA) {
            tipo = TipoMutacion.values()[rnd.nextInt(4)]; 
        }

        switch (tipo) {
            case HOIST: 
                List<NodoBloque> bloques = obtenerTodosLosBloques(hijo);
                if (bloques.size() > 1) {
                    return bloques.get(1 + rnd.nextInt(bloques.size() - 1)).clonar();
                }
                break;

            case SUB_ARBOL: 
                List<NodoBloque> blq = obtenerTodosLosBloques(hijo);
                if (!blq.isEmpty()) {
                    NodoBloque b = blq.get(rnd.nextInt(blq.size()));
                    if (!b.hijos.isEmpty()) {
                        int idx = rnd.nextInt(b.hijos.size());
                        b.hijos.set(idx, GeneradorAST.crearArbolAleatorio(0, 3));
                    }
                }
                break;

            case FUNCIONAL: 
                List<NodoCondicional> condicionales = obtenerTodosLosIF(hijo);
                if (!condicionales.isEmpty()) {
                    NodoCondicional c = condicionales.get(rnd.nextInt(condicionales.size()));
                    int r = rnd.nextInt(3);
                    if (r == 0) c.sensor = TipoSensor.values()[rnd.nextInt(TipoSensor.values().length)];
                    else if (r == 1) c.operador = Operador.values()[rnd.nextInt(Operador.values().length)];
                    else c.umbral = 10 + rnd.nextInt(90);
                }
                break;

            case TERMINAL: 
                List<NodoAccion> terminales = obtenerTodasLasAcciones(hijo);
                if (!terminales.isEmpty()) {
                    NodoAccion a = terminales.get(rnd.nextInt(terminales.size()));
                    a.accion = TipoAccion.values()[rnd.nextInt(TipoAccion.values().length)];
                }
                break;
        }
        return hijo;
    }

    // --- MÉTODOS AUXILIARES ORIGINALES ---
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