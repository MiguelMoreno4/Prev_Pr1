package source.AST;

import source.View.Mapa;
import source.View.Rover;

public class NodoCondicional implements Nodo { 
    public TipoSensor sensor;
    public Operador operador;
    public int umbral;
    

    public Nodo ramaIf;
    public Nodo ramaElse; 

    public NodoCondicional(TipoSensor sensor, Operador operador, int umbral, Nodo ramaIf, Nodo ramaElse) {
        this.sensor = sensor;
        this.operador = operador;
        this.umbral = umbral;
        this.ramaIf = ramaIf;
        this.ramaElse = ramaElse;
    }

    @Override
    public boolean ejecutar(Rover rover, Mapa mapa) { 
        int valorSensor = escanearSensor(rover, mapa);
        
        if (operador.evaluar(valorSensor, umbral)) {
            if (ramaIf != null) {
                return ramaIf.ejecutar(rover, mapa); 
            }
        } else {
            if (ramaElse != null) {
                return ramaElse.ejecutar(rover, mapa); 
            }
        }
        return false; 
    }

    // --- LÓGICA DE LOS SENSORES INTACTA ---
    private int escanearSensor(Rover rover, Mapa mapa) {
        if (sensor == TipoSensor.NIVEL_ENERGIA) return rover.bateria;

        int dist = 1;
        int cx = rover.x + rover.orientacion.dx;
        int cy = rover.y + rover.orientacion.dy;

        while (mapa.enRango(cx, cy)) {
            Mapa.TipoCasilla tipo = mapa.getCasilla(cx, cy);
            
            if (tipo == Mapa.TipoCasilla.MURO) {
                if (sensor == TipoSensor.DIST_OBSTACULO) return dist;
                return 100; 
            }
            if (sensor == TipoSensor.DIST_MUESTRA && tipo == Mapa.TipoCasilla.MUESTRA) return dist;
            if (sensor == TipoSensor.DIST_ARENA && tipo == Mapa.TipoCasilla.ARENA) return dist;
            
            dist++;
            cx += rover.orientacion.dx;
            cy += rover.orientacion.dy;
        }
        
        if (sensor == TipoSensor.DIST_OBSTACULO) return dist;
        return 100;
    }

    @Override
    public Nodo clonar() {
        // 1. Nueva instancia
        NodoCondicional copia = new NodoCondicional(this.sensor, this.operador, this.umbral, null, null);
        // 2. Copia profunda recursiva
        if (this.ramaIf != null) copia.ramaIf = this.ramaIf.clonar();
        if (this.ramaElse != null) copia.ramaElse = this.ramaElse.clonar();
        return copia;
    }

    @Override
    public String imprimir(String tab) {
        StringBuilder sb = new StringBuilder();
        sb.append(tab).append("IF ( ").append(sensor).append(" ").append(operador).append(" ").append(umbral).append(" ) {\n");
        if (ramaIf != null) sb.append(ramaIf.imprimir(tab + "  "));
        sb.append(tab).append("}\n");
        
        if (ramaElse != null) {
            sb.append(tab).append("ELSE {\n");
            sb.append(ramaElse.imprimir(tab + "  "));
            sb.append(tab).append("}\n");
        }
        return sb.toString();
    }

    @Override
    public int contarNodos() {
        int total = 1; 
        if (ramaIf != null) total += ramaIf.contarNodos();
        if (ramaElse != null) total += ramaElse.contarNodos();
        return total;
    }
}