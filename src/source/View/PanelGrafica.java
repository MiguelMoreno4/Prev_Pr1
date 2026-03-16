package source.View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelGrafica extends JPanel {

    private List<Double> mejorGen = new ArrayList<>();
    private List<Double> mejorGlobal = new ArrayList<>();
    private List<Double> media = new ArrayList<>();
    
    // Márgenes para que no se pegue a los bordes y quepa la escala
    private final int MARGEN_IZQ = 50;
    private final int MARGEN_DER = 20;
    private final int MARGEN_SUP = 20;
    private final int MARGEN_INF = 20;

    public void limpiar() {
        mejorGen.clear();
        mejorGlobal.clear();
        media.clear();
        repaint();
    }

    public void agregarDatos(double mejorG, double mejorAbs, double mediaGen) {
        mejorGen.add(mejorG);
        mejorGlobal.add(mejorAbs);
        media.add(mediaGen);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mejorGen.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - MARGEN_IZQ - MARGEN_DER;
        int h = getHeight() - MARGEN_SUP - MARGEN_INF;

        // Calculamos el máximo para escalar, pero con un pequeño margen extra (10%) 
        // para que la línea no toque el borde superior
        double max = Math.max(
        	    mejorGlobal.stream().max(Double::compare).orElse(1.0),
        	    media.stream().max(Double::compare).orElse(1.0)
        	) * 1.1;
        if (max == 0) max = 1.0;

        // --- DIBUJAR ESCALA NUMÉRICA Y EJES ---
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(MARGEN_IZQ, MARGEN_SUP, MARGEN_IZQ, MARGEN_SUP + h); // Eje Y
        g2.drawLine(MARGEN_IZQ, MARGEN_SUP + h, MARGEN_IZQ + w, MARGEN_SUP + h); // Eje X

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        int divisiones = 5;
        for (int i = 0; i <= divisiones; i++) {
            double valor = (max / divisiones) * i;
            int yPalito = MARGEN_SUP + h - (int)((valor / max) * h);
            g2.drawLine(MARGEN_IZQ - 5, yPalito, MARGEN_IZQ, yPalito); // Palito escala
            g2.drawString(String.format("%.1f", valor), 5, yPalito + 5); // Texto escala
        }

        // --- DIBUJAR LÍNEAS ---
        int n = mejorGen.size();
        for (int i = 1; i < n; i++) {
            int x1 = MARGEN_IZQ + (i - 1) * w / (n > 1 ? n - 1 : 1);
            int x2 = MARGEN_IZQ + i * w / (n > 1 ? n - 1 : 1);

            // Mejor generación (Rojo)
            g2.setColor(Color.RED);
            dibujarLineaEscalada(g2, x1, x2, mejorGen.get(i-1), mejorGen.get(i), max, h);

            // Mejor global (Azul)
            g2.setColor(Color.BLUE);
            dibujarLineaEscalada(g2, x1, x2, mejorGlobal.get(i-1), mejorGlobal.get(i), max, h);

            // Media (Verde)
            g2.setColor(Color.GREEN);
            dibujarLineaEscalada(g2, x1, x2, media.get(i-1), media.get(i), max, h);
        }
    }

    private void dibujarLineaEscalada(Graphics2D g2, int x1, int x2, double v1, double v2, double max, int h) {
        int y1 = MARGEN_SUP + h - (int)((v1 / max) * h);
        int y2 = MARGEN_SUP + h - (int)((v2 / max) * h);
        g2.drawLine(x1, y1, x2, y2);
    }
}