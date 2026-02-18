package source.View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelGrafica extends JPanel {

    private List<Double> mejorGen = new ArrayList<>();
    private List<Double> mejorGlobal = new ArrayList<>();
    private List<Double> media = new ArrayList<>();

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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        double max = mejorGlobal.stream().max(Double::compare).orElse(1.0);

        int n = mejorGen.size();

        for (int i = 1; i < n; i++) {

            int x1 = (i-1) * w / n;
            int x2 = i * w / n;

            // 🔴 Mejor generación
            g2.setColor(Color.RED);
            int y1 = h - (int)(mejorGen.get(i-1)/max * h);
            int y2 = h - (int)(mejorGen.get(i)/max * h);
            g2.drawLine(x1, y1, x2, y2);

            // 🔵 Mejor global
            g2.setColor(Color.BLUE);
            y1 = h - (int)(mejorGlobal.get(i-1)/max * h);
            y2 = h - (int)(mejorGlobal.get(i)/max * h);
            g2.drawLine(x1, y1, x2, y2);

            // 🟢 Media
            g2.setColor(Color.GREEN);
            y1 = h - (int)(media.get(i-1)/max * h);
            y2 = h - (int)(media.get(i)/max * h);
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}