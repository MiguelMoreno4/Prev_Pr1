package source.AlgoritmosGeneticos;

import java.util.*;
import source.Camaras.Camara;
import source.Individuos.Individuo;
import source.View.Mapa;
import source.View.VentanaPrincipal;

public class AlgoritmoGenetico {

    private Mapa mapa;
    private int rango;
    private int numCamaras;
    private Random rnd = new Random();
    private boolean modoPonderado = false;
    private VentanaPrincipal ventana;

    private int bitsX;
    private int bitsY;
    private int longitudCromosoma;

    public enum MetodoSeleccion { TORNEO, RULETA, ESTOCASTICO, TRUNCAMIENTO, RESTOS }
    public enum MetodoCruce { MONOPUNTO, UNIFORME }

    private MetodoSeleccion metodoSeleccion = MetodoSeleccion.TORNEO;
    private MetodoCruce metodoCruce = MetodoCruce.MONOPUNTO;

    public void setMetodoSeleccion(MetodoSeleccion metodo) { this.metodoSeleccion = metodo; }
    public void setMetodoCruce(MetodoCruce metodo) { this.metodoCruce = metodo; }

    public AlgoritmoGenetico(Mapa mapa, int rango, int numCamaras, VentanaPrincipal ventana) {
        this.mapa = mapa;
        this.rango = rango;
        this.numCamaras = numCamaras;
        this.ventana = ventana;

        bitsX = (int) Math.ceil(Math.log(mapa.columnas) / Math.log(2));
        bitsY = (int) Math.ceil(Math.log(mapa.filas) / Math.log(2));
        longitudCromosoma = numCamaras * (bitsX + bitsY);
    }

    public void setModoPonderado(boolean ponderado) {
        this.modoPonderado = ponderado;
    }

    public Individuo ejecutar(int generaciones, double probMutacion, double probCruce, double pElite) {

        ArrayList<Individuo> poblacion = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Individuo ind = crearAleatorio();
            ind.fitness = calcularFitness(ind);
            poblacion.add(ind);
        }

        Individuo mejorGlobal = poblacion.stream()
                .filter(ind -> ind.fitness > 0)
                .max(Comparator.comparingDouble(ind -> ind.fitness))
                .orElse(poblacion.get(0))
                .copiar();

        for (int g = 0; g < generaciones; g++) {

            // 🔴 Mejor de esta generación (ignorando inválidos)
        	Individuo mejorGen = poblacion.stream()
        	        .max(Comparator.comparingDouble(ind -> ind.fitness))
        	        .get();

            if (mejorGen.fitness > mejorGlobal.fitness) {
                mejorGlobal = mejorGen.copiar();
            }

            if (ventana != null) {
                double mediaGen = poblacion.stream()
                        .filter(ind -> ind.fitness > 0)
                        .mapToDouble(ind -> ind.fitness)
                        .average()
                        .orElse(mejorGlobal.fitness);

                ventana.actualizarMapaEnTiempoReal(mejorGen, g);
                ventana.actualizarGrafica(
                        mejorGen.fitness,
                        mejorGlobal.fitness,
                        mediaGen
                );
            }

            // 🔥 CREAR NUEVA GENERACIÓN
            ArrayList<Individuo> nueva = new ArrayList<>();

            // ELITISMO OPCIONAL
            if (pElite > 0) {
                int nElite = (int) Math.ceil(poblacion.size() * pElite);
                poblacion.stream()
                    .filter(ind -> ind.fitness > 0)
                    .sorted(Comparator.comparingDouble((Individuo ind) -> ind.fitness).reversed())
                    .limit(nElite)
                    .forEach(ind -> nueva.add(ind.copiar()));
            }

            while (nueva.size() < poblacion.size()) {

                Individuo padre1 = seleccionar(poblacion);
                Individuo hijo;

                if (rnd.nextDouble() < probCruce) {
                    Individuo padre2 = seleccionar(poblacion);
                    if (metodoCruce == MetodoCruce.MONOPUNTO)
                        hijo = cruceMonopunto(padre1, padre2);
                    else
                        hijo = cruceUniforme(padre1, padre2);
                } else {
                    hijo = padre1.copiar();
                }

                mutar(hijo, probMutacion);
                hijo.fitness = calcularFitness(hijo);
                nueva.add(hijo);
            }

            poblacion = nueva;
        }
        mejorGlobal.camaras = decodificar(mejorGlobal);
        return mejorGlobal;
    }

    private Individuo crearAleatorio() {
        Individuo ind = new Individuo(longitudCromosoma);
        for (int i = 0; i < longitudCromosoma; i++)
            ind.cromosoma[i] = rnd.nextBoolean();
        return ind;
    }

    private Individuo seleccionar(List<Individuo> poblacion) {
        switch (metodoSeleccion) {
            case TORNEO: return torneo(poblacion);
            case RULETA: return ruleta(poblacion);
            case ESTOCASTICO: return estocastico(poblacion);
            case TRUNCAMIENTO: return truncamiento(poblacion);
            case RESTOS: return restos(poblacion);
            default: return torneo(poblacion);
        }
    }

    private Individuo torneo(List<Individuo> poblacion) {
        Individuo a = poblacion.get(rnd.nextInt(poblacion.size()));
        Individuo b = poblacion.get(rnd.nextInt(poblacion.size()));
        return (a.fitness >= b.fitness) ? a : b;
    }

    private Individuo ruleta(List<Individuo> poblacion) {
        double totalFitness = 0;
        for (Individuo ind : poblacion) totalFitness += ind.fitness;
        double r = rnd.nextDouble() * totalFitness;
        double acum = 0;
        for (Individuo ind : poblacion) {
            acum += ind.fitness;
            if (acum >= r) return ind;
        }
        return poblacion.get(poblacion.size() - 1);
    }

    private Individuo estocastico(List<Individuo> poblacion) {
        double totalFitness = 0;
        for (Individuo ind : poblacion) totalFitness += ind.fitness;
        double r = rnd.nextDouble() * totalFitness;
        for (Individuo ind : poblacion) {
            r -= ind.fitness;
            if (r <= 0) return ind;
        }
        return poblacion.get(0);
    }

    private Individuo truncamiento(List<Individuo> poblacion) {
        int n = poblacion.size() / 2;
        return poblacion.get(rnd.nextInt(n));
    }

    private Individuo restos(List<Individuo> poblacion) {
        List<Individuo> lista = new ArrayList<>();
        for (Individuo ind : poblacion) {
            int copias = (int) ind.fitness;
            for (int i = 0; i < copias; i++) lista.add(ind);
        }
        if (lista.isEmpty()) return poblacion.get(rnd.nextInt(poblacion.size()));
        return lista.get(rnd.nextInt(lista.size()));
    }

    private Individuo cruceMonopunto(Individuo a, Individuo b) {
        Individuo hijo = new Individuo(longitudCromosoma);
        int punto = rnd.nextInt(longitudCromosoma);
        for (int i = 0; i < longitudCromosoma; i++)
            hijo.cromosoma[i] = (i < punto) ? a.cromosoma[i] : b.cromosoma[i];
        return hijo;
    }

    private Individuo cruceUniforme(Individuo a, Individuo b) {
        Individuo hijo = new Individuo(longitudCromosoma);
        for (int i = 0; i < longitudCromosoma; i++)
            hijo.cromosoma[i] = rnd.nextBoolean() ? a.cromosoma[i] : b.cromosoma[i];
        return hijo;
    }

    private void mutar(Individuo ind, double probMut) {
        for (int i = 0; i < ind.cromosoma.length; i++)
            if (rnd.nextDouble() < probMut)
                ind.cromosoma[i] = !ind.cromosoma[i];
    }

    private List<Camara> decodificar(Individuo ind) {
        List<Camara> cams = new ArrayList<>();
        int index = 0;
        for (int k = 0; k < numCamaras; k++) {
            int x = 0;
            for (int i = 0; i < bitsX; i++)
                if (ind.cromosoma[index++])
                    x += (1 << (bitsX - 1 - i));
            int y = 0;
            for (int i = 0; i < bitsY; i++)
                if (ind.cromosoma[index++])
                    y += (1 << (bitsY - 1 - i));
            x = x % mapa.columnas;
            y = y % mapa.filas;
            if (!mapa.esObstaculo(x, y))
                cams.add(new Camara(x, y));
        }
        return cams;
    }

    private double calcularFitness(Individuo ind) {
        List<Camara> cams = decodificar(ind);

        if (cams.size() != numCamaras)
            return 0;

        HashSet<String> vigiladas = new HashSet<>();
        HashSet<String> posicionesCamaras = new HashSet<>();
        for (Camara c : cams) {
            posicionesCamaras.add(c.x + "," + c.y);
        }

        for (Camara c : cams) {
            if (c.x < 0 || c.x >= mapa.columnas || c.y < 0 || c.y >= mapa.filas || mapa.esObstaculo(c.x, c.y)) {
                return 0;
            }

            vigiladas.add(c.x + "," + c.y);

            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : dirs) {
                for (int i = 1; i <= rango; i++) {
                    int nx = c.x + d[0]*i;
                    int ny = c.y + d[1]*i;

                    if (nx < 0 || nx >= mapa.columnas || ny < 0 || ny >= mapa.filas)
                        break;
                    if (mapa.esObstaculo(nx, ny))
                        break;
                    if (posicionesCamaras.contains(nx + "," + ny))
                        break;

                    vigiladas.add(nx + "," + ny);
                }
            }
        }

        return vigiladas.size();
    }

    private Individuo convertirADirecto(Individuo bin) {
        Individuo ind = new Individuo();
        ind.camaras = decodificar(bin);
        ind.fitness = bin.fitness;
        return ind;
    }
}