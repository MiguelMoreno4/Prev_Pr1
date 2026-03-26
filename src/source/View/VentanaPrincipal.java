package source.View;

import source.AST.NodoBloque;
import source.GP.AlgoritmoGenetico;
import source.GP.Evaluador;
import source.GP.GeneradorAST;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapa panelMapa; // Lo crearemos en el siguiente paso
    private PanelGrafica panelGrafica;
    
    private JButton btnEjecutar;
    private JSpinner spinPob, spinGens, spinCruce, spinMut, spinProfundidad;
    private JComboBox<String> comboMutacionOp;
    
    private JTextArea textAreaCodigo;
    private JTextPane textPaneReporteFinal;

    public VentanaPrincipal() {
        setTitle("Misión Rover Marte - Programación Genética");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 1150, 950);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===============================
        // PARAMETROS AG
        // ===============================

        JPanel pnlParams = new JPanel();
        pnlParams.setBounds(20, 10, 1090, 65);
        pnlParams.setBorder(
                BorderFactory.createTitledBorder(
                        null, "Configuración del Algoritmo Genético",
                        TitledBorder.LEADING, TitledBorder.TOP,
                        new Font("Tahoma", Font.BOLD, 11)
                )
        );
        pnlParams.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        contentPane.add(pnlParams);

        Dimension dimSpin = new Dimension(60, 22);

        spinPob = new JSpinner(new SpinnerNumberModel(100, 10, 99999, 10));
        spinPob.setPreferredSize(dimSpin);

        spinGens = new JSpinner(new SpinnerNumberModel(50, 1, 99999, 10));
        spinGens.setPreferredSize(dimSpin);

        spinCruce = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
        spinCruce.setPreferredSize(dimSpin);

        spinMut = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        spinMut.setPreferredSize(dimSpin);

        spinProfundidad = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spinProfundidad.setPreferredSize(dimSpin);

        comboMutacionOp = new JComboBox<>(new String[]{
                "Aleatoria (Las 4)", "Hoist (Poda)", "Sub-Árbol", "Funcional", "Terminal"
        });

        pnlParams.add(new JLabel("Población:")); pnlParams.add(spinPob);
        pnlParams.add(new JLabel("Generaciones:")); pnlParams.add(spinGens);
        pnlParams.add(new JLabel("Cruce %:")); pnlParams.add(spinCruce);
        pnlParams.add(new JLabel("Mutación %:")); pnlParams.add(spinMut);
        pnlParams.add(new JLabel("Profundidad Inicial:")); pnlParams.add(spinProfundidad);
        pnlParams.add(new JLabel("Tipo Mutación:")); pnlParams.add(comboMutacionOp);

        btnEjecutar = new JButton("EJECUTAR");
        btnEjecutar.setPreferredSize(new Dimension(130, 30));
        btnEjecutar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnEjecutar.setBackground(new Color(39, 174, 96));
        btnEjecutar.setForeground(Color.WHITE);
        pnlParams.add(btnEjecutar);

        // ===============================
        // MAPA
        // ===============================

        panelMapa = new PanelMapa(); // Cambiaremos PanelMapaReal por PanelMapa luego
        panelMapa.setBounds(20, 90, 520, 520);
        panelMapa.setBorder(BorderFactory.createTitledBorder("Simulación del Mejor Rover"));
        contentPane.add(panelMapa);

        // ===============================
        // PANEL REPORTE FINAL
        // ===============================

        textPaneReporteFinal = new JTextPane();
        textPaneReporteFinal.setEditable(false);
        textPaneReporteFinal.setFont(new Font("Monospaced", Font.BOLD, 14));

        JScrollPane scrollReporte = new JScrollPane(textPaneReporteFinal);
        scrollReporte.setBounds(555, 90, 555, 100);
        scrollReporte.setBorder(BorderFactory.createTitledBorder("Estadísticas del Mejor Individuo"));
        contentPane.add(scrollReporte);

        // ===============================
        // CÓDIGO GENERADO (AST)
        // ===============================
        
        textAreaCodigo = new JTextArea();
        textAreaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textAreaCodigo.setEditable(false);

        JScrollPane scrollCodigo = new JScrollPane(textAreaCodigo);
        scrollCodigo.setBounds(555, 200, 555, 410);
        scrollCodigo.setBorder(BorderFactory.createTitledBorder("Código Fuente Generado (Cerebro)"));
        contentPane.add(scrollCodigo);

        // ===============================
        // GRÁFICA
        // ===============================

        panelGrafica = new PanelGrafica(); 
        panelGrafica.setBounds(20, 620, 1090, 280);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPane.add(panelGrafica);

        // ===============================
        // LISTENERS
        // ===============================
        btnEjecutar.addActionListener(this::ejecutarAG);
    }

    private void ejecutarAG(ActionEvent e) {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                btnEjecutar.setEnabled(false);
                textAreaCodigo.setText("Evolucionando población... Espere.");
                textPaneReporteFinal.setText("");
            });

            // 1. Leer Parámetros
            int tPob = (int) spinPob.getValue();
            int tGen = (int) spinGens.getValue();
            double pCruce = (int) spinCruce.getValue() / 100.0;
            double pMut = (int) spinMut.getValue() / 100.0;
            int profMax = (int) spinProfundidad.getValue();
            
            int mutIdx = comboMutacionOp.getSelectedIndex();
            AlgoritmoGenetico.TipoMutacion tipoMut = mutIdx == 0 ? AlgoritmoGenetico.TipoMutacion.ALEATORIA : AlgoritmoGenetico.TipoMutacion.values()[mutIdx - 1];

            AlgoritmoGenetico ag = new AlgoritmoGenetico();
            List<NodoBloque> poblacion = new ArrayList<>();

            // 2. Población Inicial (Half and Half)
            for (int i = 0; i < tPob; i++) {
                boolean usarFull = (i % 2 == 0); 
                poblacion.add(GeneradorAST.generarIndividuo(profMax, usarFull));
            }

            // 3. Bucle Evolutivo
            for (int gen = 0; gen < tGen; gen++) {
                poblacion = ag.evolucionar(poblacion, tipoMut, pCruce, pMut);
                
                // Calcular estadísticas para la gráfica
                double mejorFit = 0;
                double sumaFit = 0;
                NodoBloque mejorGen = poblacion.get(0);

                for (NodoBloque ind : poblacion) {
                    double fit = Evaluador.evaluarIndividuo(ind);
                    sumaFit += fit;
                    if (fit > mejorFit) {
                        mejorFit = fit;
                        mejorGen = ind;
                    }
                }
                
                double mediaFit = sumaFit / tPob;
                final int generacionActual = gen;
                final double mF = mejorFit;
                
                // Actualizar gráfica en tiempo real (Asumiendo que panelGrafica.agregarDatos funciona como antes)
                SwingUtilities.invokeLater(() -> panelGrafica.agregarDatos(generacionActual, mF, mediaFit));
            }

            // 4. Fin de la Evolución: Extraer al mejor global
            NodoBloque mejorGlobal = poblacion.get(0);
            double maxFitness = Evaluador.evaluarIndividuo(mejorGlobal);
            for (NodoBloque ind : poblacion) {
                double fit = Evaluador.evaluarIndividuo(ind);
                if (fit > maxFitness) {
                    maxFitness = fit;
                    mejorGlobal = ind;
                }
            }

            final NodoBloque mejorFinal = mejorGlobal;
            final double fitFinal = maxFitness;

            // 5. Mostrar resultados en la UI
            SwingUtilities.invokeLater(() -> {
                textAreaCodigo.setText(mejorFinal.imprimir(""));
                textPaneReporteFinal.setText(String.format("Fitness Final: %.2f\nTamaño del Árbol (Nodos): %d\nCastigo Bloating: %.2f", 
                    fitFinal, mejorFinal.contarNodos(), mejorFinal.contarNodos() * Evaluador.COEF_BLOATING));
            });

            // 6. Simular visualmente al mejor individuo en el mapa (Semilla 3000)
            simularMejorRover(mejorFinal);

            SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));

        }).start();
    }

    private void simularMejorRover(NodoBloque mejorCerebro) {
        // Creamos un mapa y un rover frescos para la semilla base
        Mapa mapaSim = new Mapa(Evaluador.SEMILLAS[0]);
        Rover roverSim = new Rover();
        
        int tick = 0;
        while (tick < Evaluador.MAX_TICKS && !roverSim.estaApagado()) {
            mejorCerebro.ejecutar(roverSim, mapaSim);
            tick++;
            
            // Mandamos a repintar
            SwingUtilities.invokeLater(() -> panelMapa.actualizar(mapaSim, roverSim));
            
            // Pausa de 150ms para que el ojo humano pueda ver el movimiento
            try { Thread.sleep(150); } catch (InterruptedException e) {}
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}