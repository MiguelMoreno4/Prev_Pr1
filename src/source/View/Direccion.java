package source.View;

public enum Direccion {
    NORTE(0, -1), 
    ESTE(1, 0), 
    SUR(0, 1), 
    OESTE(-1, 0);

    public final int dx;
    public final int dy;

    Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direccion girarIzq() {
        // Truco matemático para retroceder en el enum circularmente
        return values()[(this.ordinal() + 3) % 4];
    }

    public Direccion girarDer() {
        // Avanzar en el enum circularmente
        return values()[(this.ordinal() + 1) % 4];
    }
}