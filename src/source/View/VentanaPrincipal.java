package source.View;

import source.AST.Nodo;
import source.GP.AlgoritmoGenetico;
import source.GP.Evaluador;
import source.GP.GeneradorAST;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapa panelMapa; 
    private PanelGrafica panelGrafica;
    
    private JButton btnEjecutar;
    // Añadimos spinBloating a la lista
    private JSpinner spinPob, spinGens, spinCruce, spinMut, spinProfundidad, spinSeed, spinBloating;
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
        
        spinSeed = new JSpinner(new SpinnerNumberModel(3000, 1, 9999999, 1));
        spinSeed.setPreferredSize(new Dimension(80, 22)); 
        
        spinPob = new JSpinner(new SpinnerNumberModel(300, 10, 99999, 10));
        spinPob.setPreferredSize(dimSpin);

        spinGens = new JSpinner(new SpinnerNumberModel(300, 1, 99999, 10));
        spinGens.setPreferredSize(dimSpin);

        spinCruce = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
        spinCruce.setPreferredSize(dimSpin);

        spinMut = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        spinMut.setPreferredSize(dimSpin);

        spinProfundidad = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spinProfundidad.setPreferredSize(dimSpin);

        // --- NUEVO SPINNER PARA BLOATING ---
        // Valor inicial: 0.5, Mínimo: 0.0, Máximo: 10.0, Paso: 0.1
        spinBloating = new JSpinner(new SpinnerNumberModel(0.5, 0.0, 10.0, 0.1));
        spinBloating.setPreferredSize(dimSpin);

        comboMutacionOp = new JComboBox<>(new String[]{
                "Aleatoria (Las 4)", "Hoist (Poda)", "Sub-Árbol", "Funcional", "Terminal"
        });
        
        pnlParams.add(new JLabel("Seed:")); pnlParams.add(spinSeed);
        pnlParams.add(new JLabel("Población:")); pnlParams.add(spinPob);
        pnlParams.add(new JLabel("Generaciones:")); pnlParams.add(spinGens);
        pnlParams.add(new JLabel("Cruce %:")); pnlParams.add(spinCruce);
        pnlParams.add(new JLabel("Mutación %:")); pnlParams.add(spinMut);
        pnlParams.add(new JLabel("Profundidad Inicial:")); pnlParams.add(spinProfundidad);
        // Añadimos el componente visual del Bloating
        pnlParams.add(new JLabel("Bloating:")); pnlParams.add(spinBloating);
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

        panelMapa = new PanelMapa(); 
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
                // OPCIONAL: Si PanelGrafica tiene un método para limpiarse, llámalo aquí.
                // panelGrafica.limpiar(); 
            });

            // 1. Leer Parámetros
            int tPob = (int) spinPob.getValue();
            int tGen = (int) spinGens.getValue();
            double pCruce = (int) spinCruce.getValue() / 100.0;
            double pMut = (int) spinMut.getValue() / 100.0;
            int profMax = (int) spinProfundidad.getValue();
            
            // --- APLICAMOS EL VALOR DEL BLOATING AL EVALUADOR ---
            Evaluador.COEF_BLOATING = ((Number) spinBloating.getValue()).doubleValue();
            
            // Leer la semilla (Si luego la vas a usar en el evaluador o Generador)
            long semilla = ((Number) spinSeed.getValue()).longValue();
            GeneradorAST.rnd.setSeed(semilla); // <--- IMPORTANTE: Aplicar la semilla
            
            int mutIdx = comboMutacionOp.getSelectedIndex();
            AlgoritmoGenetico.TipoMutacion tipoMut = mutIdx == 0 ? AlgoritmoGenetico.TipoMutacion.ALEATORIA : AlgoritmoGenetico.TipoMutacion.values()[mutIdx - 1];

            AlgoritmoGenetico ag = new AlgoritmoGenetico();
            List<Nodo> poblacion = new ArrayList<>(); // <-- CAMBIO A NODO

         // 2. Población Inicial
            poblacion = GeneradorAST.inicializarPoblacion(tPob, 2, profMax);

            // 🌟 VARIABLES PARA EL MEJOR HISTÓRICO 
            Nodo mejorNodoAbsoluto = poblacion.get(0);
            double mejorFitnessAbsoluto = Double.NEGATIVE_INFINITY;

            // 3. Bucle Evolutivo
            for (int gen = 0; gen < tGen; gen++) {
                poblacion = ag.evolucionar(poblacion, tipoMut, pCruce, pMut);
                
                // Calcular estadísticas para la gráfica
                double mejorFitGen = Double.NEGATIVE_INFINITY;
                double sumaFit = 0;

                for (Nodo ind : poblacion) {
                    double fit = Evaluador.evaluarIndividuo(ind);
                    sumaFit += fit;
                    
                    // Mejor de esta generación en concreto
                    if (fit > mejorFitGen) {
                        mejorFitGen = fit;
                    }
                    
                    // 🌟 Mejor de TODA la ejecución (Lo guardamos al vuelo)
                    if (fit > mejorFitnessAbsoluto) {
                        mejorFitnessAbsoluto = fit;
                        mejorNodoAbsoluto = ind.clonar(); // Clonamos por seguridad
                    }
                }
                
                double mediaFit = sumaFit / tPob;
                
                // Variables finales para pasarlas al hilo de Swing
                final double mG = mejorFitGen;
                final double mAbs = mejorFitnessAbsoluto;
                final double mMed = mediaFit;
                
                // ¡Corregido! Pasamos las 3 notas (Gen, Absoluta, Media)
                SwingUtilities.invokeLater(() -> panelGrafica.agregarDatos(mG, mAbs, mMed));
            }

            // 4. Fin de la Evolución: ¡Ya tenemos al mejor global extraído!
            final Nodo mejorFinal = mejorNodoAbsoluto;
            final double fitFinal = mejorFitnessAbsoluto;

            // 5. Mostrar resultados en la UI
            SwingUtilities.invokeLater(() -> {
                textAreaCodigo.setText(mejorFinal.imprimir(""));
                textPaneReporteFinal.setText(String.format("Fitness Final: %.2f\nTamaño del Árbol (Nodos): %d\nCastigo Bloating: %.2f", 
                    fitFinal, mejorFinal.contarNodos(), mejorFinal.contarNodos() * Evaluador.COEF_BLOATING));
            });

            // 6. Simular visualmente al mejor individuo en el mapa
            simularMejorRover(mejorFinal, semilla); // Le paso la semilla a la simulación

            SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));

        }).start();
    }

    private void simularMejorRover(Nodo mejorCerebro, long semilla) {
        // Creamos un mapa y un rover frescos usando la semilla
        Mapa mapaSim = new Mapa(semilla); 
        Rover roverSim = new Rover();
        
        int tick = 0;
        // Bucle de simulación: se detiene si alcanza el límite de tiempo o se queda sin batería
        while (tick < Evaluador.MAX_TICKS && !roverSim.estaApagado()) { 
            // Ejecutamos UNA iteración del árbol.
            mejorCerebro.ejecutar(roverSim, mapaSim);
            tick++;
            
            // Mandamos a repintar el mapa
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