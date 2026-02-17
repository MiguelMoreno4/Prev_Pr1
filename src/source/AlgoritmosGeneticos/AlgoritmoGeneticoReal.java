package source.AlgoritmosGeneticos;

import java.util.*;

import source.Camaras.CamaraReal;
import source.Individuos.Individuo;
import source.Individuos.IndividuoReal;
import source.View.Mapa;
import source.View.VentanaPrincipal;

public class AlgoritmoGeneticoReal {

    private Mapa mapa;
    private int rango; // alcance mÃ¡ximo de visiÃ³n
    private int numCamaras;
    private double anguloApertura; // en grados
    private boolean modoPonderado = false;
    private int[][] importancia;
    private Random rnd = new Random();
    private VentanaPrincipal ventana;

    public AlgoritmoGeneticoReal(Mapa mapa, int rango, int numCamaras, double anguloApertura, VentanaPrincipal ventana) {
        this.mapa = mapa;
        this.rango = rango;
        this.numCamaras = numCamaras;
        this.anguloApertura = anguloApertura;
        this.ventana = ventana;
    }

    public void setModoPonderado(boolean ponderado) {
        this.modoPonderado = ponderado;
    }

    public void setImportancia(int[][] importancia) {
        this.importancia = importancia;
    }

    // ===============================
    // EJECUTAR AG
    // ===============================
    public IndividuoReal ejecutar(int generaciones, double probMutacion) {

        ArrayList<IndividuoReal> poblacion = new ArrayList<>();

        // Poblacion inicial
        for (int i = 0; i < 30; i++) {
            IndividuoReal ind = crearAleatorioReal();
            ind.fitness = calcularFitness(ind);
            poblacion.add(ind);
        }

        IndividuoReal mejorGlobal = copiarIndividuo(poblacion.get(0));

        for (int g = 0; g < generaciones; g++) {

            // Ordenar por fitness descendente
            poblacion.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            IndividuoReal elite = copiarIndividuo(poblacion.get(0));

            if (elite.fitness > mejorGlobal.fitness) {
                mejorGlobal = copiarIndividuo(elite);
            }
            
            if (ventana != null && g % 5 == 0) { 
                // PASAMOS: elite.camaras (que es la List<CamaraReal>)
            	ventana.actualizarMapaRealEnTiempoReal(
            	        elite.camaras, 
            	        g, 
            	        elite.fitness, 
            	        this.rango, 
            	        this.anguloApertura
            	    );                
                try { Thread.sleep(10); } catch (Exception e) {}
            }

            ArrayList<IndividuoReal> nueva = new ArrayList<>();
            nueva.add(copiarIndividuo(elite)); // elitismo

            while (nueva.size() < poblacion.size()) {
                IndividuoReal padre = torneo(poblacion);
                IndividuoReal hijo = copiarIndividuo(padre);

                if (rnd.nextDouble() < probMutacion) {
                    mutar(hijo);
                }

                hijo.fitness = calcularFitness(hijo);
                nueva.add(hijo);
            }

            poblacion = nueva;
        }

        return copiarIndividuo(mejorGlobal);
    }
    
    /*
    private source.Individuos.Individuo convertirARealParaVisualizar(IndividuoReal real) {
        source.Individuos.Individuo visual = new source.Individuos.Individuo();
        for (CamaraReal c : real.camaras) {
            visual.camaras.add(new source.Camaras.Camara((int)Math.round(c.x), (int)Math.round(c.y)));
        }
        visual.fitness = real.fitness;
        return visual;
    }
    */
    
	private IndividuoReal crearAleatorioReal() {
        IndividuoReal ind = new IndividuoReal();
        while (ind.camaras.size() < numCamaras) {
            // Usamos el mapa dinámico que llegó al constructor
            double x = rnd.nextDouble() * (mapa.columnas - 0.01);
            double y = rnd.nextDouble() * (mapa.filas - 0.01);

            if (!mapa.esObstaculo((int)x, (int)y)) {
                double theta = rnd.nextDouble() * 360;
                ind.camaras.add(new CamaraReal(x, y, theta));
            }
        }
        return ind;
    }
	
	// ===============================
    // OPERADORES GENETICOS
    // ===============================
	
	/*
    private IndividuoReal crearAleatorioReal() {
        IndividuoReal ind = new IndividuoReal();

        while (ind.camaras.size() < numCamaras) {
            double x, y, theta;
            int xCell, yCell;
            do {
                // Multiplicamos por un valor ligeramente menor para no tocar nunca el borde exterior
                x = rnd.nextDouble() * (mapa.columnas - 0.01);
                y = rnd.nextDouble() * (mapa.filas - 0.01);

                xCell = (int)x;
                yCell = (int)y;

            // Añadimos comprobación de negativos por seguridad extrema
            } while (xCell < 0 || yCell < 0 || xCell >= mapa.columnas || 
            		yCell >= mapa.filas || mapa.esObstaculo(xCell, yCell));
            
            theta = rnd.nextDouble() * 360;
            ind.camaras.add(new CamaraReal(x, y, theta));
        }

        return ind;
    }
    */
	
	/*
    private void mutar(IndividuoReal ind) {
        int idx = rnd.nextInt(ind.camaras.size());
        CamaraReal c = ind.camaras.get(idx);

        double x, y, theta;
        int xCell, yCell;

        do {
            x = c.x + rnd.nextGaussian() * 0.5;
            y = c.y + rnd.nextGaussian() * 0.5;
            theta = (c.theta + rnd.nextGaussian() * 10) % 360;
            if (theta < 0) theta += 360;

            // limitar dentro del mapa
            x = Math.max(0, Math.min(x, mapa.columnas - 0.01));
            y = Math.max(0, Math.min(y, mapa.filas - 0.01));

            xCell = (int)x;
            yCell = (int)y;

        } while (mapa.esObstaculo(xCell, yCell));

        c.x = x;
        c.y = y;
        c.theta = theta;
    }
    */
	private void mutar(IndividuoReal ind) {
	    int idx = rnd.nextInt(ind.camaras.size());
	    CamaraReal c = ind.camaras.get(idx);

	    double nx, ny, nt;
	    int intentos = 0;
	    boolean valido = false;

	    do {
	        // Mutación pequeña (Gaussiana)
	        nx = c.x + rnd.nextGaussian() * 1.0; 
	        ny = c.y + rnd.nextGaussian() * 1.0;
	        nt = (c.theta + rnd.nextGaussian() * 15) % 360;
	        if (nt < 0) nt += 360;

	        // Limitar dentro del mapa
	        nx = Math.max(0, Math.min(nx, mapa.columnas - 0.01));
	        ny = Math.max(0, Math.min(ny, mapa.filas - 0.01));

	        // Validar si es suelo libre
	        if (!mapa.esObstaculo((int)nx, (int)ny)) {
	            valido = true;
	        }

	        intentos++;
	        // Si se queda atrapada en una pared (muchos intentos), 
	        // la lanzamos a un punto aleatorio del mapa para "rescatarla"
	        if (intentos > 50 && !valido) {
	            nx = rnd.nextDouble() * (mapa.columnas - 0.01);
	            ny = rnd.nextDouble() * (mapa.filas - 0.01);
	            if (!mapa.esObstaculo((int)nx, (int)ny)) valido = true;
	        }
	    } while (!valido);

	    c.x = nx;
	    c.y = ny;
	    c.theta = nt;
	}
	
    private IndividuoReal torneo(List<IndividuoReal> poblacion) {
        IndividuoReal a = poblacion.get(rnd.nextInt(poblacion.size()));
        IndividuoReal b = poblacion.get(rnd.nextInt(poblacion.size()));
        return (a.fitness >= b.fitness) ? a : b;
    }

    private IndividuoReal copiarIndividuo(IndividuoReal ind) {
        IndividuoReal copia = new IndividuoReal();
        for (CamaraReal c : ind.camaras) {
            copia.camaras.add(new CamaraReal(c.x, c.y, c.theta));
        }
        copia.fitness = ind.fitness;
        return copia;
    }

    // ===============================
    // FITNESS CON VISION CONICA
    // ===============================
    public double calcularFitness(IndividuoReal ind) {

        HashSet<String> vigiladas = new HashSet<>();
        double fitness = 0.0;

        for (CamaraReal c : ind.camaras) {

            int minX = (int)Math.max(0, Math.floor(c.x - rango));
            int maxX = (int)Math.min(mapa.columnas - 1, Math.ceil(c.x + rango));
            int minY = (int)Math.max(0, Math.floor(c.y - rango));
            int maxY = (int)Math.min(mapa.filas - 1, Math.ceil(c.y + rango));

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {

                    if (mapa.esObstaculo(x, y)) continue;

                    // DISTANCIA
                    double dx = x + 0.5 - c.x;
                    double dy = y + 0.5 - c.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist > rango) continue;

                    // ANGULO
                    double ang = Math.toDegrees(Math.atan2(dy, dx));
                    double diff = Math.abs(ang - c.theta);
                    diff = Math.min(diff, 360 - diff);
                    if (diff > anguloApertura/2) continue;

                    // LÃ�NEA DE VISION (raycast simple)
                    if (!lineaVision(c.x, c.y, x + 0.5, y + 0.5)) continue;
                    
                    String key = x + "," + y;
                    if (!vigiladas.contains(key)) {
                        vigiladas.add(key);
                        fitness += modoPonderado ? importancia[y][x] : 1;
                    }
                }
            }
        }

        return fitness;
    }
    
    
    // ===============================
    // RAYCAST SIMPLE (Bresenham) MODIFICADO
    // ===============================
    private boolean lineaVision(double x0, double y0, double x1, double y1) {
        // Calculamos la dirección y distancia total
        double dx = x1 - x0;
        double dy = y1 - y0;
        double distancia = Math.sqrt(dx * dx + dy * dy);

        // Definimos un paso muy pequeño (0.2 unidades de celda) 
        // para asegurar que no nos saltamos ningún muro fino
        double paso = 0.2; 
        int numPasos = (int) (distancia / paso);

        double xActual = x0;
        double yActual = y0;

        // Avanzamos por el rayo desde la cámara al objetivo
        for (int i = 1; i <= numPasos; i++) {
            xActual += (dx / distancia) * paso;
            yActual += (dy / distancia) * paso;

            int cx = (int) Math.floor(xActual);
            int cy = (int) Math.floor(yActual);

            // Si el rayo toca un muro antes de llegar al centro de la celda objetivo...
            if (mapa.esObstaculo(cx, cy)) {
                // Si la celda que tocamos es justo la celda destino, la visión es correcta
                if (cx == (int)Math.floor(x1) && cy == (int)Math.floor(y1)) {
                    return true;
                }
                // Si es un muro intermedio, bloqueamos la visión
                return false; 
            }
        }
        return true;
    }
    /*
    // ===============================
    // RAYCAST SIMPLE (Bresenham)
    // ===============================
    private boolean lineaVision(double x0, double y0, double x1, double y1) {
        int ix0 = (int)Math.floor(x0);
        int iy0 = (int)Math.floor(y0);
        int ix1 = (int)Math.floor(x1);
        int iy1 = (int)Math.floor(y1);

        int dx = Math.abs(ix1 - ix0);
        int dy = Math.abs(iy1 - iy0);

        int sx = ix0 < ix1 ? 1 : -1;
        int sy = iy0 < iy1 ? 1 : -1;

        int err = dx - dy;

        int x = ix0;
        int y = iy0;

        while (true) {
        	if (x != ix0 || y != iy0) { 
                if (mapa.esObstaculo(x, y)) return false;
            }
            if (x == ix1 && y == iy1) break;

            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx)  { err += dx; y += sy; }
            
            // Seguridad: evitar bucles infinitos por errores de redondeo
            if (x < 0 || x >= mapa.columnas || y < 0 || y >= mapa.filas) return false;
        }

        return true;
    }
    */
}