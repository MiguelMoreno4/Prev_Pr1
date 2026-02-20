package source.AlgoritmosGeneticos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import source.Camaras.CamaraReal;
import source.Individuos.IndividuoReal;
import source.View.VentanaPrincipal;
import source.View.Mapa;

public class AlgoritmoGeneticoReal {

    private Mapa mapa;
    private int rango;
    private int numCamaras;
    private double apertura;
    private VentanaPrincipal ventana;
    private Random random = new Random();
    
    private int tamPoblacion = 100;
    private double probCruce = 0.6;
    private double probMutacion = 0.05;
    private double porcentajeElite = 0.05;
    private boolean modoPonderado = false;

    public AlgoritmoGeneticoReal(Mapa mapa, int rango, int numCamaras, double apertura, VentanaPrincipal ventana) {
        this.mapa = mapa;
        this.rango = rango;
        this.numCamaras = numCamaras;
        this.apertura = apertura;
        this.ventana = ventana;
    }

    public void setConfig(int pob, double cruce, double mut, double elite) {
        this.tamPoblacion = pob;
        this.probCruce = cruce;
        this.probMutacion = mut;
        this.porcentajeElite = elite;
    }

    public void setModoPonderado(boolean valor) {
        this.modoPonderado = valor;
    }

    public IndividuoReal ejecutar(int generaciones) {
        List<IndividuoReal> poblacion = crearPoblacionInicial();
        IndividuoReal mejorAbsoluto = null;

        for (int g = 0; g < generaciones; g++) {
            for (IndividuoReal ind : poblacion) {
                ind.fitness = calcularFitness(ind);
            }

            poblacion.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            
            if (mejorAbsoluto == null || poblacion.get(0).fitness > mejorAbsoluto.fitness) {
                mejorAbsoluto = poblacion.get(0).copiar(); // Usamos tu método copiar()
            }

            if (ventana != null) {
                ventana.actualizarMapaRealEnTiempoReal(poblacion.get(0).camaras, g, poblacion.get(0).fitness, rango, apertura);
                ventana.actualizarGrafica(poblacion.get(0).fitness, mejorAbsoluto.fitness, calcularMedia(poblacion));
            }
            
            try {
                Thread.sleep(10); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            List<IndividuoReal> nuevaPob = new ArrayList<>();
            int numElite = (int) (tamPoblacion * porcentajeElite);
            for (int i = 0; i < numElite; i++) {
                nuevaPob.add(poblacion.get(i).copiar());
            }

            while (nuevaPob.size() < tamPoblacion) {
                IndividuoReal p1 = seleccionar(poblacion);
                IndividuoReal p2 = seleccionar(poblacion);
                
                IndividuoReal hijo = (random.nextDouble() < probCruce) ? cruceAritmetico(p1, p2) : p1.copiar();
                
                if (random.nextDouble() < probMutacion) {
                    mutar(hijo);
                }
                nuevaPob.add(hijo);
            }
            poblacion = nuevaPob;
        }
        return mejorAbsoluto;
    }

    private List<IndividuoReal> crearPoblacionInicial() {
        List<IndividuoReal> pob = new ArrayList<>();
        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoReal ind = new IndividuoReal(); // Usamos tu constructor vacío
            for (int j = 0; j < numCamaras; j++) {
                ind.camaras.add(generarCamaraValida());
            }
            pob.add(ind);
        }
        return pob;
    }

    private CamaraReal generarCamaraValida() {
        int x, y;
        do {
            x = random.nextInt(mapa.columnas);
            y = random.nextInt(mapa.filas);
        } while (mapa.esObstaculo(x, y));
        
        return new CamaraReal(x, y, random.nextDouble() * 360);
    }
    
    private double calcularFitness(IndividuoReal ind) {
        boolean[][] visto = new boolean[mapa.filas][mapa.columnas];
        double score = 0;

        for (CamaraReal c : ind.camaras) {
            // 1. Usamos a += 1.0 para no saltarnos ningún muro por error
            for (double a = c.theta - apertura / 2; a <= c.theta + apertura / 2; a += 1.0) {
                
                // 2. Usamos pasos de distancia más cortos (0.5) para detectar muros mejor
                for (double d = 1.0; d <= rango; d += 0.5) {
                    int vx = (int) Math.floor(c.x + Math.cos(Math.toRadians(a)) * d);
                    int vy = (int) Math.floor(c.y + Math.sin(Math.toRadians(a)) * d);

                    // Límites
                    if (vx < 0 || vx >= mapa.columnas || vy < 0 || vy >= mapa.filas) break;
                    
                    // Muros: Si detectamos muro, este rayo muere
                    if (mapa.esObstaculo(vx, vy)) break;

                    // Solo contamos si la distancia d es un entero (para no contar doble)
                    if (d % 1.0 == 0 && !visto[vy][vx]) {
                        visto[vy][vx] = true;
                        score += modoPonderado ? mapa.getValorImportancia(vx, vy) : 1;
                    }
                }
            }
        }
        return score;
    }
    private IndividuoReal seleccionar(List<IndividuoReal> pob) {
        IndividuoReal mejor = null;
        for (int i = 0; i < 3; i++) {
            IndividuoReal cand = pob.get(random.nextInt(pob.size()));
            if (mejor == null || cand.fitness > mejor.fitness) mejor = cand;
        }
        return mejor;
    }

    private IndividuoReal cruceAritmetico(IndividuoReal p1, IndividuoReal p2) {
        IndividuoReal hijo = new IndividuoReal();
        double alpha = random.nextDouble();
        for (int i = 0; i < numCamaras; i++) {
            CamaraReal c1 = p1.camaras.get(i);
            CamaraReal c2 = p2.camaras.get(i);
            double nx = c1.x * alpha + c2.x * (1 - alpha);
            double ny = c1.y * alpha + c2.y * (1 - alpha);
            double nt = c1.theta * alpha + c2.theta * (1 - alpha);
            hijo.camaras.add(new CamaraReal(nx, ny, nt));
        }
        return hijo;
    }

    private void mutar(IndividuoReal ind) {
        int idx = random.nextInt(ind.camaras.size());
        CamaraReal c = ind.camaras.get(idx);
        
        double nuevaX = c.x + (random.nextDouble() * 2 - 1);
        double nuevaY = c.y + (random.nextDouble() * 2 - 1);

        // Solo aplicamos el cambio si la nueva posición es suelo (0)
        if (nuevaX >= 0 && nuevaX < mapa.columnas && nuevaY >= 0 && nuevaY < mapa.filas) {
            if (!mapa.esObstaculo((int)nuevaX, (int)nuevaY)) {
                c.x = nuevaX;
                c.y = nuevaY;
            }
        }
        c.theta = (c.theta + random.nextGaussian() * 15) % 360;
    }
    private double calcularMedia(List<IndividuoReal> pob) {
        double sum = 0;
        for (IndividuoReal ind : pob) sum += ind.fitness;
        return sum / pob.size();
    }
}