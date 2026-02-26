package source.AlgoritmosGeneticos;

import java.util.*;
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

    public enum MetodoSeleccion { TORNEO, RULETA, ESTOCASTICO, TRUNCAMIENTO, RESTOS }
    private MetodoSeleccion metodoSeleccion = MetodoSeleccion.TORNEO;
    
    public enum MetodoCruce { MONOPUNTO, UNIFORME, ARITMETICO, BLX_ALPHA }
    public enum MetodoMutacion { GEN, GAUSSIANA }
    
    private MetodoCruce metodoCruce = MetodoCruce.ARITMETICO;
    private MetodoMutacion metodoMutacion = MetodoMutacion.GAUSSIANA;
    private double blxAlpha = 0.5; // Para BLX-α
    
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

    public void setModoPonderado(boolean valor) { this.modoPonderado = valor; }
    public void setMetodoSeleccion(MetodoSeleccion m) { this.metodoSeleccion = m; }
    public void setMetodoCruce(MetodoCruce m) { this.metodoCruce = m; }
    public void setMetodoMutacion(MetodoMutacion m) { this.metodoMutacion = m; }
    public void setBlxAlpha(double a) { this.blxAlpha = a; }
    
    public IndividuoReal ejecutar(int generaciones) {

        List<IndividuoReal> poblacion = crearPoblacionInicial();

        //  Mejor absoluto
        IndividuoReal mejorGlobal = poblacion.stream()
                .max(Comparator.comparingDouble(ind -> ind.fitness))
                .orElse(poblacion.get(0))
                .copiar();

        for (int g = 0; g < generaciones; g++) {

            // 1️ Calcular fitness
            for (IndividuoReal ind : poblacion) {
                ind.fitness = calcularFitness(ind);
            }

            // Mejor de esta generación
            IndividuoReal mejorGen = poblacion.stream()
                    .max(Comparator.comparingDouble(ind -> ind.fitness))
                    .get();

            // Actualizar mejor absoluto
            if (mejorGen.fitness > mejorGlobal.fitness) {
                mejorGlobal = mejorGen.copiar();
            }

            // Media de la generación 
            double mediaGen = poblacion.stream()
                    .mapToDouble(ind -> ind.fitness)
                    .average()
                    .orElse(0.0);

            // Actualizar GUI
            if (ventana != null) {
                ventana.actualizarMapaRealEnTiempoReal(mejorGen.camaras, g, mejorGen.fitness, rango, apertura);
                ventana.actualizarGrafica(
                        mejorGen.fitness,    //  mejor de la generación
                        mejorGlobal.fitness, //  mejor absoluto
                        mediaGen             //  media
                );
            }

            //  Crear nueva generación
            List<IndividuoReal> nuevaPob = new ArrayList<>();
            int numElite = (int) (tamPoblacion * porcentajeElite);
            for (int i = 0; i < numElite; i++) nuevaPob.add(poblacion.get(i).copiar());

            while (nuevaPob.size() < tamPoblacion) {
                IndividuoReal p1 = seleccionar(poblacion);
                IndividuoReal p2 = seleccionar(poblacion);
                IndividuoReal hijo = (random.nextDouble() < probCruce) ? cruzar(p1, p2) : p1.copiar();
                if (random.nextDouble() < probMutacion) mutar(hijo);
                nuevaPob.add(hijo);
            }

            poblacion = nuevaPob;
        }

        return mejorGlobal;
    }

    private List<IndividuoReal> crearPoblacionInicial() {
        List<IndividuoReal> pob = new ArrayList<>();
        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoReal ind = new IndividuoReal();
            for (int j = 0; j < numCamaras; j++) ind.camaras.add(generarCamaraValida());
            pob.add(ind);
        }
        return pob;
    }

    private CamaraReal generarCamaraValida() {
        double x, y;
        do {
            x = random.nextDouble() * mapa.columnas;
            y = random.nextDouble() * mapa.filas;
        } while (mapa.esObstaculo((int)x, (int)y));
        return new CamaraReal(x, y, random.nextDouble() * 360);
    }

    // ---------------- CALCULO FITNESS CORRECTO ----------------
    private double calcularFitness(IndividuoReal ind) {
        boolean[][] visto = new boolean[mapa.filas][mapa.columnas];
        double score = 0;

        for (CamaraReal c : ind.camaras) {
            // Rango de celdas a revisar (cuadrado)
            int minX = Math.max(0, (int)Math.floor(c.x - rango));
            int maxX = Math.min(mapa.columnas - 1, (int)Math.ceil(c.x + rango));
            int minY = Math.max(0, (int)Math.floor(c.y - rango));
            int maxY = Math.min(mapa.filas - 1, (int)Math.ceil(c.y + rango));

            for (int vy = minY; vy <= maxY; vy++) {
                for (int vx = minX; vx <= maxX; vx++) {
                    // 1. Distancia
                    double dx = vx + 0.5 - c.x;
                    double dy = vy + 0.5 - c.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist > rango) continue;

                    // 2. Ángulo
                    double angCelda = Math.toDegrees(Math.atan2(dy, dx));
                    double angDif = Math.abs((angCelda - c.theta + 360) % 360);
                    if (angDif > apertura/2 && angDif < 360 - apertura/2) continue;

                    // 3. Raycast (línea de visión)
                    if (!lineaDeVision(c.x, c.y, vx + 0.5, vy + 0.5)) continue;

                    // Marcar visible
                    if (!visto[vy][vx]) {
                        visto[vy][vx] = true;
                        score += modoPonderado ? mapa.getValorImportancia(vx, vy) : 1;
                    }
                }
            }
        }

        return score;
    }

    // Método auxiliar: raycast desde (x0,y0) hasta (x1,y1)
    private boolean lineaDeVision(double x0, double y0, double x1, double y1) {
        int steps = (int)(Math.ceil(Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)) * 2)); 
        for (int i = 1; i <= steps; i++) {
            double t = (double)i / steps;
            int xi = (int)Math.floor(x0 + (x1 - x0) * t);
            int yi = (int)Math.floor(y0 + (y1 - y0) * t);
            if (xi < 0 || xi >= mapa.columnas || yi < 0 || yi >= mapa.filas) return false;
            if (mapa.esObstaculo(xi, yi)) return false;
        }
        return true;
    }

    private boolean hayObstaculoEnLinea(double x0, double y0, double x1, double y1, IndividuoReal ind) {
        int pasos = (int) Math.ceil(Math.max(Math.abs(x1-x0), Math.abs(y1-y0)) * 2);
        for (int i = 0; i <= pasos; i++) {
            double t = i / (double) pasos;
            double x = x0 + (x1 - x0) * t;
            double y = y0 + (y1 - y0) * t;
            int ix = (int)Math.floor(x);
            int iy = (int)Math.floor(y);

            if (ix < 0 || ix >= mapa.columnas || iy < 0 || iy >= mapa.filas) return true;
            if (mapa.esObstaculo(ix, iy)) return true;

            for (CamaraReal c : ind.camaras) {
                if (c.x >= ix && c.x < ix+1 && c.y >= iy && c.y < iy+1) return true;
            }
        }
        return false;
    }

    // ---------------- SELECCIÓN ----------------
    private IndividuoReal seleccionar(List<IndividuoReal> pob) {
        switch (metodoSeleccion) {
            case TORNEO: return seleccionarTorneo(pob);
            case RULETA: return seleccionarRuleta(pob);
            case ESTOCASTICO: return seleccionarEstocastico(pob);
            case TRUNCAMIENTO: return seleccionarTruncamiento(pob);
            case RESTOS: return seleccionarRestos(pob);
            default: return seleccionarTorneo(pob);
        }
    }

    private IndividuoReal seleccionarTorneo(List<IndividuoReal> pob) {
        IndividuoReal mejor = null;
        for (int i = 0; i < 3; i++) {
            IndividuoReal cand = pob.get(random.nextInt(pob.size()));
            if (mejor == null || cand.fitness > mejor.fitness) mejor = cand;
        }
        return mejor;
    }

    private IndividuoReal seleccionarRuleta(List<IndividuoReal> pob) {
        double total = pob.stream().mapToDouble(ind -> ind.fitness).sum();
        double r = random.nextDouble() * total;
        double acum = 0;
        for (IndividuoReal ind : pob) {
            acum += ind.fitness;
            if (acum >= r) return ind;
        }
        return pob.get(pob.size()-1);
    }

    private IndividuoReal seleccionarEstocastico(List<IndividuoReal> pob) {
        double total = pob.stream().mapToDouble(ind -> ind.fitness).sum();
        double p = total / pob.size();
        double r = random.nextDouble() * total;
        double acum = 0;
        for (IndividuoReal ind : pob) {
            acum += ind.fitness;
            if (acum >= r) return ind;
        }
        return pob.get(pob.size()-1);
    }

    private IndividuoReal seleccionarTruncamiento(List<IndividuoReal> pob) {
        pob.sort((a,b)->Double.compare(b.fitness, a.fitness));
        int top = (int)(pob.size() * 0.5);
        return pob.get(random.nextInt(top));
    }

    private IndividuoReal seleccionarRestos(List<IndividuoReal> pob) {
        List<IndividuoReal> lista = new ArrayList<>();
        double total = pob.stream().mapToDouble(ind -> ind.fitness).sum();
        for (IndividuoReal ind : pob) {
            int n = (int) Math.floor((ind.fitness / total) * pob.size());
            for (int i=0;i<n;i++) lista.add(ind);
        }
        while (lista.size() < pob.size()) lista.add(pob.get(random.nextInt(pob.size())));
        return lista.get(random.nextInt(lista.size()));
    }

    // ---------------- CRUCE ----------------
    private IndividuoReal cruzar(IndividuoReal p1, IndividuoReal p2) {
        switch(metodoCruce) {
            case MONOPUNTO: return cruceMonopunto(p1, p2);
            case UNIFORME: return cruceUniforme(p1, p2);
            case ARITMETICO: return cruceAritmetico(p1, p2);
            case BLX_ALPHA: return cruceBLXAlpha(p1, p2);
            default: return p1.copiar();
        }
    }

    private IndividuoReal cruceMonopunto(IndividuoReal p1, IndividuoReal p2) {
        IndividuoReal hijo = new IndividuoReal();
        int punto = random.nextInt(numCamaras);
        for (int i = 0; i < numCamaras; i++) {
            hijo.camaras.add(i < punto ? p1.camaras.get(i).copiar() : p2.camaras.get(i).copiar());
        }
        return hijo;
    }

    private IndividuoReal cruceUniforme(IndividuoReal p1, IndividuoReal p2) {
        IndividuoReal hijo = new IndividuoReal();
        for (int i = 0; i < numCamaras; i++) {
            CamaraReal c = (random.nextBoolean() ? p1.camaras.get(i) : p2.camaras.get(i)).copiar();
            hijo.camaras.add(c);
        }
        return hijo;
    }

    private IndividuoReal cruceBLXAlpha(IndividuoReal p1, IndividuoReal p2) {
        IndividuoReal hijo = new IndividuoReal();
        for (int i = 0; i < numCamaras; i++) {
            CamaraReal c1 = p1.camaras.get(i);
            CamaraReal c2 = p2.camaras.get(i);
            double minX = Math.min(c1.x, c2.x);
            double maxX = Math.max(c1.x, c2.x);
            double minY = Math.min(c1.y, c2.y);
            double maxY = Math.max(c1.y, c2.y);
            double minT = Math.min(c1.theta, c2.theta);
            double maxT = Math.max(c1.theta, c2.theta);

            double dx = maxX - minX;
            double dy = maxY - minY;
            double dt = maxT - minT;

            double nx = minX - blxAlpha*dx + random.nextDouble() * (dx*(1+2*blxAlpha));
            double ny = minY - blxAlpha*dy + random.nextDouble() * (dy*(1+2*blxAlpha));
            double nt = minT - blxAlpha*dt + random.nextDouble() * (dt*(1+2*blxAlpha));

            hijo.camaras.add(new CamaraReal(nx, ny, nt));
        }
        return hijo;
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

    // ---------------- MUTACIÓN ----------------
    private void mutar(IndividuoReal ind) {
        switch(metodoMutacion) {
            case GEN: mutarGen(ind); break;
            case GAUSSIANA: mutarGaussiana(ind); break;
        }
    }

    private void mutarGen(IndividuoReal ind) {
        int idx = random.nextInt(ind.camaras.size());
        CamaraReal c = ind.camaras.get(idx);

        double nuevaX = c.x + (random.nextDouble()*2 - 1);
        double nuevaY = c.y + (random.nextDouble()*2 - 1);
        if (nuevaX >= 0 && nuevaX < mapa.columnas && nuevaY >=0 && nuevaY < mapa.filas)
            if (!mapa.esObstaculo((int)nuevaX,(int)nuevaY)) { c.x=nuevaX; c.y=nuevaY; }
        c.theta = (c.theta + random.nextDouble()*60 - 30) % 360; // ±30° aleatorio
    }

    private void mutarGaussiana(IndividuoReal ind) {
        int idx = random.nextInt(ind.camaras.size());
        CamaraReal c = ind.camaras.get(idx);

        double nuevaX = c.x + random.nextGaussian();
        double nuevaY = c.y + random.nextGaussian();
        if (nuevaX >= 0 && nuevaX < mapa.columnas && nuevaY >=0 && nuevaY < mapa.filas)
            if (!mapa.esObstaculo((int)nuevaX,(int)nuevaY)) { c.x=nuevaX; c.y=nuevaY; }
        c.theta = (c.theta + random.nextGaussian()*15) % 360;
    }
    private double calcularMedia(List<IndividuoReal> pob) {
        return pob.stream().mapToDouble(ind -> ind.fitness).average().orElse(0);
    }
}