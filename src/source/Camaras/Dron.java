package source.Camaras;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Dron {
    private double velocidad;       // Velocidad del dron (1.5x, 1.0x, etc.)
    private List<Camara> ruta;      // Lista de puntos/cámaras asignadas
    private Color color;            // Color para dibujar en la GUI

    // Nuevo: posición base del dron
    private int baseX;
    private int baseY;

    public Dron(double velocidad, Color color) {
        this.velocidad = velocidad;
        this.color = color;
        this.ruta = new ArrayList<>();
        this.baseX = 0;  // Por defecto, inicio en (0,0)
        this.baseY = 0;
    }

    // Getters y setters
    public double getVelocidad() { return velocidad; }
    public void setVelocidad(double velocidad) { this.velocidad = velocidad; }

    public List<Camara> getRuta() { return ruta; }
    public void setRuta(List<Camara> ruta) { this.ruta = ruta; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public void agregarPunto(Camara p) { ruta.add(p); }

    // Getters y setters para la base
    public int getBaseX() { return baseX; }
    public void setBaseX(int baseX) { this.baseX = baseX; }

    public int getBaseY() { return baseY; }
    public void setBaseY(int baseY) { this.baseY = baseY; }
}