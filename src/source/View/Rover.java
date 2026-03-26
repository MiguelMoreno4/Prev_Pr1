package source.View;

public class Rover {
    // Estado físico
    public int x;
    public int y;
    public Direccion orientacion;
    public int bateria;

    // Estadísticas para calcular el Fitness (Según PDF)
    public int muestrasRecolectadas = 0;
    public int casillasExploradas = 0;
    public int recompensaVisual = 0;
    public int pasosArena = 0;
    public int colisiones = 0;
    
    // Control de trampas (Reward Hacking)
    private int girosConsecutivos = 0;
    private boolean[][] mapaExplorado;

    public Rover() {
        this.x = 1; // Fila 1 según PDF
        this.y = 1; // Columna 1 según PDF
        this.orientacion = Direccion.ESTE; // Orientado al Este
        this.bateria = 100; // Energía inicial
        
        // Matriz de 15x15 para recordar por dónde ha pasado
        this.mapaExplorado = new boolean[Mapa.COLUMNAS][Mapa.FILAS];
        registrarExploracion(this.x, this.y); // La casilla de salida ya cuenta como explorada
    }

    // --- MECÁNICAS DE ACCIÓN (Llamadas desde los nodos hoja del AST) ---

    public void avanzar(Mapa mapa) {
        this.girosConsecutivos = 0; // Si avanza, se le cura el "mareo"
        
        int nx = this.x + orientacion.dx;
        int ny = this.y + orientacion.dy;

        Mapa.TipoCasilla casillaFrontal = mapa.getCasilla(nx, ny);

        if (casillaFrontal == Mapa.TipoCasilla.MURO) {
            // Choca y no se mueve
            this.bateria -= 2;
            this.colisiones++;
        } else {
            // Se mueve
            this.x = nx;
            this.y = ny;
            registrarExploracion(this.x, this.y);

            if (casillaFrontal == Mapa.TipoCasilla.ARENA) {
                this.bateria -= 10;
                this.pasosArena++;
            } else if (casillaFrontal == Mapa.TipoCasilla.MUESTRA) {
                this.bateria -= 1; // Moverse cuesta 1
                this.muestrasRecolectadas++;
                mapa.recogerMuestra(nx, ny); // Borra la muestra del mapa
            } else {
                // Suelo normal
                this.bateria -= 1;
            }
        }
    }

    public void girarIzq() {
        this.orientacion = orientacion.girarIzq();
        this.bateria -= 1;
        registrarGiro();
    }

    public void girarDer() {
        this.orientacion = orientacion.girarDer();
        this.bateria -= 1;
        registrarGiro();
    }

    // --- MÉTODOS DE CONTROL Y FITNESS ---

    private void registrarGiro() {
        girosConsecutivos++;
        // Castigo por bucle (Mareo): 4 giros sin avanzar = -20E
        if (girosConsecutivos >= 4) {
            this.bateria -= 20;
            girosConsecutivos = 0; // Reseteamos para que no le reste 20 cada giro extra
        }
    }

    private void registrarExploracion(int px, int py) {
        if (!mapaExplorado[px][py]) {
            mapaExplorado[px][py] = true;
            casillasExploradas++;
        }
    }

    public boolean estaApagado() {
        return bateria <= 0;
    }

    // Aplica la fórmula exacta de la página 3 del enunciado
    public double calcularFitnessBase() {
        double fitness = (muestrasRecolectadas * 500) 
                       + (casillasExploradas * 20) 
                       + (recompensaVisual * 2) 
                       - (pasosArena * 30) 
                       - (colisiones * 10);

        // Penalización por pereza (explorar menos de 4 casillas)
        if (casillasExploradas < 4) {
            fitness -= 1000;
        }

        return fitness;
    }

	public int getX() {
		// TODO Auto-generated method stub
		return x;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return y;
	}

	public Direccion getDireccion() {
		// TODO Auto-generated method stub
		return this.orientacion;
	}
}