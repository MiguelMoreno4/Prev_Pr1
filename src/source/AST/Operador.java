package source.AST;

public enum Operador {
    MENOR, MAYOR, IGUAL;
    
    public boolean evaluar(int valorSensor, int umbral) {
        switch (this) {
            case MENOR: return valorSensor < umbral;
            case MAYOR: return valorSensor > umbral;
            case IGUAL: return valorSensor == umbral;
            default: return false;
        }
    }
}
