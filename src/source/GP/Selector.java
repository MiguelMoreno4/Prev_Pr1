package source.GP;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import source.AST.Nodo;

public class Selector {

    private Random rnd = new Random();

    /**
     * Selector principal para el Algoritmo Genético de Árboles (AST)
     */
    public Nodo seleccionar(List<Nodo> poblacion, String metodo, Map<Nodo, Double> cache) {
        switch(metodo.toUpperCase()) {
            case "TORNEO":
                return seleccionTorneo(poblacion, 2, cache); // torneo de 2 individuos
            case "RULETA":
                return seleccionRuleta(poblacion, cache);
            case "ESTOCASTICO":
                return seleccionEstocasticaUniversal(poblacion, cache);
            case "RESTOS":
                return seleccionPorRestos(poblacion, cache);
            case "RANKING":
                return seleccionRanking(poblacion, cache);
            case "TRUNCAMIENTO":
                return seleccionTruncamiento(poblacion, 0.5, cache); // top 50%
            default:
                return poblacion.get(rnd.nextInt(poblacion.size())).clonar();
        }
    }

    /** 1️ TORNEO simple (Se queda con el MAYOR fitness) */
    private Nodo seleccionTorneo(List<Nodo> poblacion, int k, Map<Nodo, Double> cache) {
        Nodo mejor = null;
        double mejorFitness = -Double.MAX_VALUE;
        
        for (int i = 0; i < k; i++) {
            Nodo cand = poblacion.get(rnd.nextInt(poblacion.size()));
            double fitCand = cache.get(cand);
            if (mejor == null || fitCand > mejorFitness) {
                mejor = cand;
                mejorFitness = fitCand;
            }
        }
        return mejor.clonar();
    }

    /** 2️ RULETA directa (mayor fitness = más probabilidad) */
    private Nodo seleccionRuleta(List<Nodo> poblacion, Map<Nodo, Double> cache) {
        // Sumamos el fitness real (no el inverso)
        double total = poblacion.stream().mapToDouble(cache::get).sum();
        
        // Protección por si toda la población tiene fitness 0
        if (total <= 0) return poblacion.get(rnd.nextInt(poblacion.size())).clonar(); 
        
        double r = rnd.nextDouble() * total;
        double acum = 0;
        for (Nodo i : poblacion) {
            acum += cache.get(i);
            if (acum >= r) return i.clonar();
        }
        return poblacion.get(0).clonar();
    }

    /** 3️ ESTOCASTICO UNIVERSAL (SUS adaptado a fitness positivo) */
    private Nodo seleccionEstocasticaUniversal(List<Nodo> poblacion, Map<Nodo, Double> cache) {
        double total = poblacion.stream().mapToDouble(cache::get).sum();
        if (total <= 0) return poblacion.get(rnd.nextInt(poblacion.size())).clonar();
        
        double puntero = rnd.nextDouble() * total;
        double paso = total / poblacion.size();
        double acum = 0;
        
        for (Nodo i : poblacion) {
            acum += cache.get(i);
            if (acum >= puntero) return i.clonar();
            puntero += paso;
        }
        return poblacion.get(0).clonar();
    }

    /** 4️ POR RESTOS (proporción entera calculada con el fitness real) */
    private Nodo seleccionPorRestos(List<Nodo> poblacion, Map<Nodo, Double> cache) {
        double total = poblacion.stream().mapToDouble(cache::get).sum();
        if (total <= 0) return poblacion.get(rnd.nextInt(poblacion.size())).clonar();
        
        List<Nodo> seleccion = new ArrayList<>();
        for (Nodo i : poblacion) {
            // Regla de 3 directa
            double valor = (cache.get(i) / total) * poblacion.size(); 
            int enteros = (int) valor;
            for (int e = 0; e < enteros; e++) seleccion.add(i.clonar());
        }
        
        // Rellenar si la parte decimal dejó huecos
        while (seleccion.isEmpty() || seleccion.size() < poblacion.size()) {
            seleccion.add(poblacion.get(rnd.nextInt(poblacion.size())).clonar());
        }
        return seleccion.get(rnd.nextInt(seleccion.size()));
    }

    /** 5️ RANKING (Probabilidad basada en posición, no en nota) */
    private Nodo seleccionRanking(List<Nodo> poblacion, Map<Nodo, Double> cache) {
        List<Nodo> ordenada = new ArrayList<>(poblacion);
        // Orden DESCENDENTE: El índice 0 es el de mayor fitness
        ordenada.sort((a, b) -> Double.compare(cache.get(b), cache.get(a)));
        
        int n = ordenada.size();
        double total = n * (n + 1) / 2.0;
        double r = rnd.nextDouble();
        double acumulado = 0;
        
        for (int i = 0; i < n; i++) {
            // El índice 0 (el mejor) se lleva la porción más grande: (n - 0) / total
            acumulado += (n - i) / total; 
            if (r <= acumulado) return ordenada.get(i).clonar();
        }
        return ordenada.get(n - 1).clonar();
    }

    /** 6️ TRUNCAMIENTO (Selección aleatoria dentro del TOP p%) */
    private Nodo seleccionTruncamiento(List<Nodo> poblacion, double p, Map<Nodo, Double> cache) {
        // Aseguramos que al menos coge 1 individuo
        int n = Math.max(1, (int) (p * poblacion.size())); 
        List<Nodo> ordenada = new ArrayList<>(poblacion);
        
        // Orden DESCENDENTE: Los mejores arriba
        ordenada.sort((a, b) -> Double.compare(cache.get(b), cache.get(a)));
        
        return ordenada.get(rnd.nextInt(n)).clonar();
    }
}