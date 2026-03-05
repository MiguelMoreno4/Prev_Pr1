package source.AlgoritmosGeneticos;

import java.util.*;
import java.awt.Color;

import source.Camaras.Camara;
import source.Camaras.Dron;
import source.Camaras.FabricaDrones;
import source.Individuos.IndividuoMTSP;
import source.View.AStar;
import source.View.Mapa;
import source.View.VentanaPrincipal;

public class AlgoritmoGeneticoMTSP {

    private Mapa mapa;
    private List<Camara> puntosControl;
    private int numDrones;
    private List<Dron> flota;
    private AStar aStar;
    private VentanaPrincipal ventana;
    private Random rnd;

    public AlgoritmoGeneticoMTSP(Mapa mapa,
                                 List<Camara> puntosControl,
                                 int numDrones,
                                 long semilla,
                                 VentanaPrincipal ventana) {
        this.mapa = mapa;
        this.puntosControl = puntosControl;
        this.numDrones = numDrones;
        this.ventana = ventana;
        this.rnd = new Random(semilla);
        this.aStar = new AStar(mapa);
        this.flota = FabricaDrones.crearFlota(numDrones);
    }

    public List<Dron> getFlota() {
        return this.flota;
    }

    /**
     * Convierte una lista de cámaras en un IndividuoMTSP
     */
    public IndividuoMTSP convertirACromosomaMTSP(List<Camara> camaras) {

        List<Integer> cromosoma = new ArrayList<>();

        int C = camaras.size();
        int D = numDrones;

        // cámaras
        for(int i = 1; i <= C; i++)
            cromosoma.add(i);

        // separadores
        for(int i = C+1; i <= C + D - 1; i++)
            cromosoma.add(i);

        return new IndividuoMTSP(cromosoma);
    }

    /**
     * Decodifica un IndividuoMTSP en rutas reales de drones (lista de listas de índices)
     */
    public List<List<Integer>> decodificar(IndividuoMTSP ind) {

        List<List<Integer>> rutas = new ArrayList<>();

        for(int i=0;i<numDrones;i++)
            rutas.add(new ArrayList<>());

        int dronActual = 0;
        int C = puntosControl.size();

        for(int gen : ind.cromosoma){

            if(gen > C){  
                // separador
                dronActual++;

                if(dronActual >= numDrones)
                    dronActual = numDrones - 1;
            }
            else{
                // cámara
                rutas.get(dronActual).add(gen - 1);
            }
        }

        return rutas;
    }

    /**
     * Calcula el "fitness" de un individuo MTSP (tiempo máximo entre drones)
     */
    public double calcularFitness(IndividuoMTSP ind) {

        List<List<Integer>> rutas = decodificar(ind);

        double maxT = 0;
        double minT = Double.MAX_VALUE;

        Set<String> posicionesCamaras = new HashSet<>();
        for (Camara c : puntosControl)
            posicionesCamaras.add(c.x + "," + c.y);

        for (int d = 0; d < rutas.size(); d++) {

            Dron dron = flota.get(d);
            List<Integer> ruta = rutas.get(d);

            double tiempoTotal = 0;

            int xActual = dron.getBaseX();
            int yActual = dron.getBaseY();

            for (int idCam : ruta) {

                Camara destino = puntosControl.get(idCam);

                double coste = aStar.calcularCoste(
                        xActual, yActual,
                        destino.x, destino.y,
                        posicionesCamaras
                );

                if (coste == Double.POSITIVE_INFINITY)
                    return Double.MAX_VALUE;

                tiempoTotal += coste / dron.getVelocidad();

                xActual = destino.x;
                yActual = destino.y;
            }

            double costeVuelta = aStar.calcularCoste(
                    xActual, yActual,
                    dron.getBaseX(), dron.getBaseY(),
                    posicionesCamaras
            );

            tiempoTotal += costeVuelta / dron.getVelocidad();

            maxT = Math.max(maxT, tiempoTotal);
            minT = Math.min(minT, tiempoTotal);
        }

        double penalizacion = (maxT - minT) * 0.5;

        return maxT + penalizacion;
    }

    /**
     * Método simple de prueba para ejecutar MTSP con rutas de AG
     */
    public IndividuoMTSP ejecutarSimulacion() {

        IndividuoMTSP mejor = null;
        double mejorFitness = Double.MAX_VALUE;

        // probar varios individuos aleatorios
        for(int i=0;i<200;i++){

            IndividuoMTSP ind = crearIndividuoAleatorio();
            ind.fitness = calcularFitness(ind);

            if(ind.fitness < mejorFitness){
                mejorFitness = ind.fitness;
                mejor = ind;
            }
        }
        imprimirRutasDrones(mejor);
        return mejor;
    }
    public IndividuoMTSP crearIndividuoAleatorio() {

        int C = puntosControl.size();
        int D = numDrones;

        List<Integer> genes = new ArrayList<>();

        // cámaras
        for(int i = 1; i <= C; i++)
            genes.add(i);

        // separadores
        for(int i = C + 1; i <= C + D - 1; i++)
            genes.add(i);

        Collections.shuffle(genes, rnd);

        return new IndividuoMTSP(genes);
    }
    public void imprimirRutasDrones(IndividuoMTSP ind) {

        List<List<Integer>> rutas = decodificar(ind);

        System.out.println("===== RUTAS DRONES =====");

        for(int d = 0; d < rutas.size(); d++){

            System.out.print("Dron " + (d+1) + ": Base -> ");

            for(int idCam : rutas.get(d)){
                Camara c = puntosControl.get(idCam);
                System.out.print("(" + c.x + "," + c.y + ") -> ");
            }

            System.out.println("Base");
        }

        System.out.println("========================");
        System.out.println("Fitness" + ind.fitness);
    }
    /**
     * Cruce PMX para dos padres, devuelve dos hijos
     */
    public IndividuoMTSP[] crucePMX(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int size = padre1.cromosoma.size();
        IndividuoMTSP hijo1 = new IndividuoMTSP(new ArrayList<>(Collections.nCopies(size, -1)));
        IndividuoMTSP hijo2 = new IndividuoMTSP(new ArrayList<>(Collections.nCopies(size, -1)));

        // Elegir dos puntos de corte aleatorios
        int corte1 = rnd.nextInt(size);
        int corte2 = rnd.nextInt(size);
        if (corte1 > corte2) { int tmp = corte1; corte1 = corte2; corte2 = tmp; }

        // Paso 1: copiar la franja central cruzada
        for (int i = corte1; i <= corte2; i++) {
            hijo1.cromosoma.set(i, padre1.cromosoma.get(i));
            hijo2.cromosoma.set(i, padre2.cromosoma.get(i));
        }

        // Paso 2: rellenar el resto de las posiciones
        for (int i = 0; i < size; i++) {
            if (i >= corte1 && i <= corte2) continue;

            // Rellenar hijo1 usando padre2
            int candidato1 = padre2.cromosoma.get(i);
            while (hijo1.cromosoma.contains(candidato1)) {
                int indice_conflicto = padre2.cromosoma.indexOf(candidato1);
                candidato1 = padre1.cromosoma.get(indice_conflicto);
            }
            hijo1.cromosoma.set(i, candidato1);

            // Rellenar hijo2 usando padre1
            int candidato2 = padre1.cromosoma.get(i);
            while (hijo2.cromosoma.contains(candidato2)) {
                int indice_conflicto = padre1.cromosoma.indexOf(candidato2);
                candidato2 = padre2.cromosoma.get(indice_conflicto);
            }
            hijo2.cromosoma.set(i, candidato2);
        }

        return new IndividuoMTSP[]{hijo1, hijo2};
    }
    /**
     * Cruce OX (Order Crossover) para dos padres, devuelve dos hijos
     */
    public IndividuoMTSP[] cruceOX(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int size = padre1.cromosoma.size();
        IndividuoMTSP hijo1 = new IndividuoMTSP(new ArrayList<>(Collections.nCopies(size, -1)));
        IndividuoMTSP hijo2 = new IndividuoMTSP(new ArrayList<>(Collections.nCopies(size, -1)));

        // Elegir dos puntos de corte aleatorios
        int corte1 = rnd.nextInt(size);
        int corte2 = rnd.nextInt(size);
        if (corte1 > corte2) { int tmp = corte1; corte1 = corte2; corte2 = tmp; }

        // Paso 1: copiar la franja central directamente
        for (int i = corte1; i <= corte2; i++) {
            hijo1.cromosoma.set(i, padre1.cromosoma.get(i));
            hijo2.cromosoma.set(i, padre2.cromosoma.get(i));
        }

        // Paso 2: extraer elementos de los padres que no estén en la franja
        List<Integer> elementosHijo1 = new ArrayList<>();
        List<Integer> elementosHijo2 = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (!hijo1.cromosoma.contains(padre2.cromosoma.get(i)))
                elementosHijo1.add(padre2.cromosoma.get(i));
            if (!hijo2.cromosoma.contains(padre1.cromosoma.get(i)))
                elementosHijo2.add(padre1.cromosoma.get(i));
        }

        // Paso 3: rellenar los huecos en orden circular
        int idxH1 = 0, idxH2 = 0;
        for (int i = 0; i < size; i++) {
            if (i >= corte1 && i <= corte2) continue; // saltar la franja central
            hijo1.cromosoma.set(i, elementosHijo1.get(idxH1++));
            hijo2.cromosoma.set(i, elementosHijo2.get(idxH2++));
        }

        return new IndividuoMTSP[]{hijo1, hijo2};
    }
    /**
     * Cruce OXPP (Order Crossover con Dos Puntos)
     * @param padre1 Cromosoma del primer padre
     * @param padre2 Cromosoma del segundo padre
     * @return Array de dos hijos
     */
    public IndividuoMTSP[] cruceOXPP(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int n = padre1.cromosoma.size();
        List<Integer> c1 = padre1.cromosoma;
        List<Integer> c2 = padre2.cromosoma;

        // Seleccionar dos puntos de corte aleatorios
        int corte1 = rnd.nextInt(n - 1);
        int corte2 = corte1 + 1 + rnd.nextInt(n - corte1 - 1); // corte2 > corte1

        List<Integer> hijo1 = new ArrayList<>(Collections.nCopies(n, -1));
        List<Integer> hijo2 = new ArrayList<>(Collections.nCopies(n, -1));

        // Paso 1: copiar la franja central
        for (int i = corte1; i <= corte2; i++) {
            hijo1.set(i, c1.get(i));
            hijo2.set(i, c2.get(i));
        }

        // Paso 2: rellenar los huecos preservando el orden relativo del otro padre
        rellenarOXPP(hijo1, c2, corte1, corte2);
        rellenarOXPP(hijo2, c1, corte1, corte2);

        return new IndividuoMTSP[] {
            new IndividuoMTSP(hijo1),
            new IndividuoMTSP(hijo2)
        };
    }

    /**
     * Método auxiliar que rellena los huecos de un hijo OXPP
     */
    private void rellenarOXPP(List<Integer> hijo, List<Integer> padre, int corte1, int corte2) {
        int n = hijo.size();
        int idxHijo = (corte2 + 1) % n;

        for (int i = 0; i < n; i++) {
            int gen = padre.get((corte2 + 1 + i) % n);

            // saltar si ya está en el hijo
            if (hijo.contains(gen)) continue;

            // colocar el gen en el primer hueco disponible
            while (hijo.get(idxHijo) != -1) {
                idxHijo = (idxHijo + 1) % n;
            }

            hijo.set(idxHijo, gen);
            idxHijo = (idxHijo + 1) % n;
        }
    }
    /**
     * Cruce por Ciclos (CX) para dos padres, devuelve dos hijos
     */
    public IndividuoMTSP[] cruceCX(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int size = padre1.cromosoma.size();
        IndividuoMTSP hijo1 = new IndividuoMTSP(new ArrayList<>(Collections.nCopies(size, -1)));
        IndividuoMTSP hijo2 = new IndividuoMTSP(new ArrayList<>(Collections.nCopies(size, -1)));

        boolean[] visitado = new boolean[size]; // marcar posiciones ya asignadas

        int ciclo = 0;

        // Paso 1: Encontrar ciclos y asignar a los hijos
        for (int start = 0; start < size; start++) {
            if (visitado[start]) continue;

            int index = start;
            do {
                hijo1.cromosoma.set(index, (ciclo % 2 == 0) ? padre1.cromosoma.get(index) : padre2.cromosoma.get(index));
                hijo2.cromosoma.set(index, (ciclo % 2 == 0) ? padre2.cromosoma.get(index) : padre1.cromosoma.get(index));
                visitado[index] = true;

                int valorPadre2 = padre2.cromosoma.get(index);
                index = padre1.cromosoma.indexOf(valorPadre2);
            } while (index != start);

            ciclo++;
        }

        return new IndividuoMTSP[]{hijo1, hijo2};
    }
    /**
     * Recombinación de Rutas (ERX) para dos padres
     */
    public IndividuoMTSP[] cruceERX(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int size = padre1.cromosoma.size();
        
        // Construir mapas de vecinos para ambos padres
        Map<Integer, Set<Integer>> mapaAristas1 = construirMapaVecinos(padre1);
        Map<Integer, Set<Integer>> mapaAristas2 = construirMapaVecinos(padre2);

        IndividuoMTSP hijo1 = new IndividuoMTSP(construirHijoERX(padre1, mapaAristas1));
        IndividuoMTSP hijo2 = new IndividuoMTSP(construirHijoERX(padre2, mapaAristas2));

        return new IndividuoMTSP[]{hijo1, hijo2};
    }

    /** Construye mapa de vecinos de un cromosoma */
    private Map<Integer, Set<Integer>> construirMapaVecinos(IndividuoMTSP padre) {
        Map<Integer, Set<Integer>> mapa = new HashMap<>();
        List<Integer> crom = padre.cromosoma;
        int n = crom.size();

        for (int i = 0; i < n; i++) {
            int gene = crom.get(i);
            Set<Integer> vecinos = new HashSet<>();
            vecinos.add(crom.get((i - 1 + n) % n)); // anterior
            vecinos.add(crom.get((i + 1) % n));     // siguiente
            mapa.put(gene, vecinos);
        }
        return mapa;
    }

    /** Construir hijo a partir de un padre inicial y un mapa de vecinos */
    private List<Integer> construirHijoERX(IndividuoMTSP padre, Map<Integer, Set<Integer>> mapaAristas) {
        List<Integer> hijo = new ArrayList<>();
        Set<Integer> visitados = new HashSet<>();

        int actual = padre.cromosoma.get(0);
        hijo.add(actual);
        visitados.add(actual);

        while (hijo.size() < padre.cromosoma.size()) {
            // Eliminar referencias al nodo actual
            for (Set<Integer> vecinos : mapaAristas.values()) {
                vecinos.remove(actual);
            }

            Set<Integer> vecinos = mapaAristas.get(actual);
            int siguiente;

            if (!vecinos.isEmpty()) {
                // Elegir vecino con menos conexiones restantes
                siguiente = vecinos.stream()
                        .min(Comparator.comparingInt(g -> mapaAristas.get(g).size()))
                        .get();
            } else {
                // Elegir un gene aleatorio no visitado
                List<Integer> noVisitados = new ArrayList<>();
                for (int g : padre.cromosoma) if (!visitados.contains(g)) noVisitados.add(g);
                siguiente = noVisitados.get(rnd.nextInt(noVisitados.size()));
            }

            hijo.add(siguiente);
            visitados.add(siguiente);
            actual = siguiente;
        }

        return hijo;
    }
    /**
     * Cruce por Codificación Ordinal (CO)
     */
    public IndividuoMTSP[] cruceCO(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int n = padre1.cromosoma.size();

        // Paso 1: convertir a ordinal
        int[] ordinal1 = convertirAOrdinal(padre1.cromosoma);
        int[] ordinal2 = convertirAOrdinal(padre2.cromosoma);

        // Paso 2: cruce de 1 punto sobre ordinales
        int puntoCorte = 1 + rnd.nextInt(n - 1); // entre 1 y n-1

        int[] hijoOrdinal1 = new int[n];
        int[] hijoOrdinal2 = new int[n];

        for (int i = 0; i < n; i++) {
            if (i < puntoCorte) {
                hijoOrdinal1[i] = ordinal1[i];
                hijoOrdinal2[i] = ordinal2[i];
            } else {
                hijoOrdinal1[i] = ordinal2[i];
                hijoOrdinal2[i] = ordinal1[i];
            }
        }

        // Paso 3: convertir de vuelta a permutación
        List<Integer> cromHijo1 = convertirALista(hijoOrdinal1);
        List<Integer> cromHijo2 = convertirALista(hijoOrdinal2);

        return new IndividuoMTSP[]{new IndividuoMTSP(cromHijo1), new IndividuoMTSP(cromHijo2)};
    }

    /** Convierte una permutación a ordinal */
    private int[] convertirAOrdinal(List<Integer> perm) {
        int n = perm.size();
        List<Integer> temp = new ArrayList<>(perm);
        int[] ordinal = new int[n];

        for (int i = 0; i < n; i++) {
            ordinal[i] = temp.indexOf(perm.get(i));
            temp.remove(perm.get(i));
        }
        return ordinal;
    }

    /** Convierte un array ordinal a permutación */
    private List<Integer> convertirALista(int[] ordinal) {
        int n = ordinal.length;
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < n; i++) temp.add(i + 1); // suponemos 1..N o adaptarlo según MTSP
        List<Integer> perm = new ArrayList<>();

        for (int o : ordinal) {
            perm.add(temp.get(o));
            temp.remove(o);
        }
        return perm;
    }
    
    /**
     * Cruce inventado: Greedy Swap Crossover (GSC)
     * Tomamos los cromosomas de los padres.

Dividimos en dos mitades.

El hijo 1 toma la primera mitad del padre 1 y luego va rellenando con los genes del padre 2 en orden, evitando duplicados.

El hijo 2 hace lo mismo al revés.

Esto preserva parcialmente la estructura de rutas de cada padre y evita drones vacíos
     */
    public IndividuoMTSP[] cruceGSC(IndividuoMTSP padre1, IndividuoMTSP padre2) {
        int n = padre1.cromosoma.size();
        List<Integer> c1 = padre1.cromosoma;
        List<Integer> c2 = padre2.cromosoma;

        List<Integer> hijo1 = new ArrayList<>(Collections.nCopies(n, -1));
        List<Integer> hijo2 = new ArrayList<>(Collections.nCopies(n, -1));

        int mitad = n / 2;

        // Hijo1: primera mitad de padre1
        for (int i = 0; i < mitad; i++) {
            hijo1.set(i, c1.get(i));
        }

        // Rellenar hijo1 con genes de padre2 sin duplicados
        int idx = mitad;
        for (int i = 0; i < n; i++) {
            int gen = c2.get(i);
            if (!hijo1.contains(gen)) {
                hijo1.set(idx, gen);
                idx++;
                if (idx >= n) break;
            }
        }

        // Hijo2: primera mitad de padre2
        for (int i = 0; i < mitad; i++) {
            hijo2.set(i, c2.get(i));
        }

        // Rellenar hijo2 con genes de padre1 sin duplicados
        idx = mitad;
        for (int i = 0; i < n; i++) {
            int gen = c1.get(i);
            if (!hijo2.contains(gen)) {
                hijo2.set(idx, gen);
                idx++;
                if (idx >= n) break;
            }
        }

        return new IndividuoMTSP[] { new IndividuoMTSP(hijo1), new IndividuoMTSP(hijo2) };
    } 
    
    
    /**
     * Selección de individuos para AG MTSP
     */
    public IndividuoMTSP seleccionar(List<IndividuoMTSP> poblacion, String metodo) {
        switch(metodo.toUpperCase()) {
            case "TORNEO":
                return seleccionTorneo(poblacion, 2); // torneo de 2 individuos
            case "RULETA":
                return seleccionRuleta(poblacion);
            case "ESTOCASTICO":
                return seleccionEstocasticaUniversal(poblacion);
            case "RESTOS":
                return seleccionPorRestos(poblacion);
            case "RANKING":
                return seleccionRanking(poblacion);
            case "TRUNCAMIENTO":
                return seleccionTruncamiento(poblacion, 0.5); // top 50%
            default:
                return poblacion.get(rnd.nextInt(poblacion.size()));
        }
    }

    /** 1️⃣ TORNEO simple */
    private IndividuoMTSP seleccionTorneo(List<IndividuoMTSP> poblacion, int k) {
        IndividuoMTSP mejor = null;
        for (int i = 0; i < k; i++) {
            IndividuoMTSP cand = poblacion.get(rnd.nextInt(poblacion.size()));
            if (mejor == null || cand.fitness < mejor.fitness)
                mejor = cand;
        }
        return mejor.copiar();
    }

    /** 2️⃣ RULETA inversa (mejor fitness = más probabilidad) */
    private IndividuoMTSP seleccionRuleta(List<IndividuoMTSP> poblacion) {
        double total = poblacion.stream().mapToDouble(i -> 1.0 / i.fitness).sum();
        double r = rnd.nextDouble() * total;
        double acum = 0;
        for (IndividuoMTSP i : poblacion) {
            acum += 1.0 / i.fitness;
            if (acum >= r) return i.copiar();
        }
        return poblacion.get(0).copiar();
    }

    /** 3️⃣ ESTOCASTICO UNIVERSAL (SUS) */
    private IndividuoMTSP seleccionEstocasticaUniversal(List<IndividuoMTSP> poblacion) {
        double total = poblacion.stream().mapToDouble(i -> 1.0 / i.fitness).sum();
        double puntero = rnd.nextDouble() * total;
        double paso = total / poblacion.size();
        double acum = 0;
        for (IndividuoMTSP i : poblacion) {
            acum += 1.0 / i.fitness;
            if (acum >= puntero) return i.copiar();
            puntero += paso;
        }
        return poblacion.get(0).copiar();
    }

    /** 4️⃣ POR RESTOS (proporción entera de selección) */
    private IndividuoMTSP seleccionPorRestos(List<IndividuoMTSP> poblacion) {
        double total = poblacion.stream().mapToDouble(i -> 1.0 / i.fitness).sum();
        List<IndividuoMTSP> seleccion = new ArrayList<>();
        for (IndividuoMTSP i : poblacion) {
            double valor = (1.0 / i.fitness) * poblacion.size() / total;
            int enteros = (int) valor;
            for (int e = 0; e < enteros; e++) seleccion.add(i.copiar());
        }
        // rellenar si no alcanza
        while (seleccion.isEmpty() || seleccion.size() < poblacion.size()) {
            seleccion.add(poblacion.get(rnd.nextInt(poblacion.size())).copiar());
        }
        return seleccion.get(rnd.nextInt(seleccion.size()));
    }

    /** 5️⃣ RANKING */
    private IndividuoMTSP seleccionRanking(List<IndividuoMTSP> poblacion) {
        List<IndividuoMTSP> ordenada = new ArrayList<>(poblacion);
        ordenada.sort(Comparator.comparingDouble(i -> i.fitness));
        int n = ordenada.size();
        double total = n * (n + 1) / 2.0;
        double r = rnd.nextDouble();
        double acumulado = 0;
        for (int i = 0; i < n; i++) {
            acumulado += (n - i) / total;
            if (r <= acumulado) return ordenada.get(i).copiar();
        }
        return ordenada.get(n - 1).copiar();
    }

    /** 6️⃣ TRUNCAMIENTO (selecciona de los mejores p%) */
    private IndividuoMTSP seleccionTruncamiento(List<IndividuoMTSP> poblacion, double p) {
        int n = (int) (p * poblacion.size());
        List<IndividuoMTSP> ordenada = new ArrayList<>(poblacion);
        ordenada.sort(Comparator.comparingDouble(i -> i.fitness));
        return ordenada.get(rnd.nextInt(n)).copiar();
    }
    /**
     * MUTACIONES PARA AG MTSP
     */
    public void mutar(IndividuoMTSP ind, String tipo) {
        switch(tipo.toUpperCase()) {
            case "INSERCION":
                mutacionInsercion(ind);
                break;
            case "INTERCAMBIO":
                mutacionIntercambio(ind);
                break;
            case "INVERSIÓN":
                mutacionInversion(ind);
                break;
            case "HEURISTICA":
                mutacionHeuristica(ind);
                break;
            case "PROPIA":
                mutacionPropia(ind);
                break;
            default:
                mutacionIntercambio(ind);
        }
    }

    /** 1️⃣ Inserción: toma un gen aleatorio y lo inserta en otra posición */
    private void mutacionInsercion(IndividuoMTSP ind) {
        int size = ind.cromosoma.size();
        int from = rnd.nextInt(size);
        int to = rnd.nextInt(size);
        if (from == to) return;

        int valor = ind.cromosoma.remove(from);
        ind.cromosoma.add(to, valor);
    }

    /** 2️⃣ Intercambio: intercambia dos genes aleatorios */
    private void mutacionIntercambio(IndividuoMTSP ind) {
        int size = ind.cromosoma.size();
        int i = rnd.nextInt(size);
        int j = rnd.nextInt(size);
        Collections.swap(ind.cromosoma, i, j);
    }

    /** 3️⃣ Inversión: invierte un tramo del cromosoma */
    private void mutacionInversion(IndividuoMTSP ind) {
        int size = ind.cromosoma.size();
        int i = rnd.nextInt(size);
        int j = rnd.nextInt(size);
        if (i > j) { int tmp = i; i = j; j = tmp; }
        while (i < j) {
            Collections.swap(ind.cromosoma, i, j);
            i++; j--;
        }
    }

    /** 4️⃣ Heurística: mueve el gen más lejano de la base al principio del dron más cercano */
    private void mutacionHeuristica(IndividuoMTSP ind) {
        List<Integer> genes = new ArrayList<>();
        int nCamaras = puntosControl.size();

        // extraemos solo genes de cámaras (ignorando separadores)
        for (int g : ind.cromosoma) if (g <= nCamaras) genes.add(g);

        if (genes.size() < 2) return;

        // tomamos 2 cámaras aleatorias y las intercambiamos si están lejos de la base
        int a = genes.get(rnd.nextInt(genes.size()));
        int b = genes.get(rnd.nextInt(genes.size()));
        if (a == b) return;

        // intercambiar posiciones en el cromosoma original
        int idxA = ind.cromosoma.indexOf(a);
        int idxB = ind.cromosoma.indexOf(b);
        Collections.swap(ind.cromosoma, idxA, idxB);
    }

    /** 5️⃣ Mutación propia: invertir aleatoriamente un tramo entre separadores */
    private void mutacionPropia(IndividuoMTSP ind) {
        int size = ind.cromosoma.size();
        List<Integer> separadores = new ArrayList<>();
        int nCamaras = puntosControl.size();

        // identificar índices de separadores
        for (int i = 0; i < size; i++)
            if (ind.cromosoma.get(i) > nCamaras) separadores.add(i);

        // elegir tramo aleatorio entre separadores o entre inicio/fin
        int start = (separadores.isEmpty() ? 0 : rnd.nextInt(separadores.get(0) + 1));
        int end = (separadores.isEmpty() ? size - 1 : rnd.nextInt(size - separadores.get(separadores.size()-1)) + separadores.get(separadores.size()-1));
        if (start >= end) return;

        while (start < end) {
            Collections.swap(ind.cromosoma, start, end);
            start++; end--;
        }
    }
    /**
     * Aplica elitismo sobre la población
     * @param poblacion Actual población de individuos
     * @param porcentajeElitismo Valor entre 0 y 1 que indica el porcentaje de individuos a preservar
     * @return Lista de individuos elitistas que se copiarán a la siguiente generación
     */
    private List<IndividuoMTSP> aplicarElitismo(List<IndividuoMTSP> poblacion, double porcentajeElitismo) {
        int nElitismo = (int) Math.ceil(poblacion.size() * porcentajeElitismo);
        if (nElitismo <= 0) return new ArrayList<>();

        // Ordenar por fitness ascendente (menor fitness es mejor)
        List<IndividuoMTSP> ordenada = new ArrayList<>(poblacion);
        ordenada.sort(Comparator.comparingDouble(i -> i.fitness));

        // Tomar los mejores
        List<IndividuoMTSP> elitistas = new ArrayList<>();
        for (int i = 0; i < nElitismo; i++) {
            elitistas.add(ordenada.get(i).copiar());
        }
        return elitistas;
    }
}