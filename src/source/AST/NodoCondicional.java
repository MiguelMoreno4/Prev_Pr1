package source.AST;

import source.View.Mapa;
import source.View.Rover;

public class NodoCondicional implements Nodo {
    public TipoSensor sensor;
    public Operador operador;
    public int umbral;
    
    public NodoBloque ramaIf;
    public NodoBloque ramaElse; // Puede ser null

    public NodoCondicional(TipoSensor sensor, Operador operador, int umbral) {
        this.sensor = sensor;
        this.operador = operador;
        this.umbral = umbral;
        this.ramaIf = new NodoBloque();
        this.ramaElse = new NodoBloque();
    }

    @Override
    public boolean ejecutar(Rover rover, Mapa mapa) {
        int valorSensor = escanearSensor(rover, mapa);
        
        if (operador.evaluar(valorSensor, umbral)) {
            return ramaIf.ejecutar(rover, mapa);
        } else if (ramaElse != null && !ramaElse.instrucciones.isEmpty()) {
            return ramaElse.ejecutar(rover, mapa);
        }
        return false;
    }

    // --- LÓGICA DE LOS SENSORES SEGÚN EL PDF ---
    private int escanearSensor(Rover rover, Mapa mapa) {
        if (sensor == TipoSensor.NIVEL_ENERGIA) {
            return rover.bateria;
        }

        // Para los espaciales, escaneamos en línea recta
        int dist = 1;
        int cx = rover.x + rover.orientacion.dx;
        int cy = rover.y + rover.orientacion.dy;

        while (mapa.enRango(cx, cy)) {
            Mapa.TipoCasilla tipo = mapa.getCasilla(cx, cy);
            
            if (tipo == Mapa.TipoCasilla.MURO) {
                if (sensor == TipoSensor.DIST_OBSTACULO) return dist;
                return 100; // La vista se bloquea, devuelve 100 para Muestra/Arena
            }
            if (sensor == TipoSensor.DIST_MUESTRA && tipo == Mapa.TipoCasilla.MUESTRA) return dist;
            if (sensor == TipoSensor.DIST_ARENA && tipo == Mapa.TipoCasilla.ARENA) return dist;
            
            dist++;
            cx += rover.orientacion.dx;
            cy += rover.orientacion.dy;
        }
        
        // Si salimos del mapa, cuenta como obstáculo
        if (sensor == TipoSensor.DIST_OBSTACULO) return dist;
        return 100;
    }

    @Override
    public Nodo clonar() {
        NodoCondicional copia = new NodoCondicional(sensor, operador, umbral);
        copia.ramaIf = (NodoBloque) this.ramaIf.clonar();
        if (this.ramaElse != null) copia.ramaElse = (NodoBloque) this.ramaElse.clonar();
        return copia;
    }

    @Override
    public String imprimir(String tab) {
        StringBuilder sb = new StringBuilder();
        sb.append(tab).append("IF ( ").append(sensor).append(" ").append(operador).append(" ").append(umbral).append(" ) {\n");
        sb.append(ramaIf.imprimir(tab + "  "));
        sb.append(tab).append("}\n");
        
        if (ramaElse != null && !ramaElse.instrucciones.isEmpty()) {
            sb.append(tab).append("ELSE {\n");
            sb.append(ramaElse.imprimir(tab + "  "));
            sb.append(tab).append("}\n");
        }
        return sb.toString();
    }

    @Override
    public int contarNodos() {
        int total = 1;
        total += ramaIf.contarNodos();
        if (ramaElse != null) total += ramaElse.contarNodos();
        return total;
    }
}