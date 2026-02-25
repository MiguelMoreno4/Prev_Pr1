package source.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import source.AlgoritmosGeneticos.AlgoritmoGenetico;
import source.AlgoritmosGeneticos.AlgoritmoGeneticoReal;
import source.Camaras.Camara;
import source.Camaras.CamaraReal;
import source.Escenarios.EscenarioDatos;
import source.Escenarios.EscenariosFactory;
import source.Individuos.Individuo;
import source.Individuos.IndividuoReal;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapaReal panelMapa; 
    private JButton btnEjecutar;
    private JCheckBox chckbxPonderado;
    private JComboBox<String> comboEscenario;
    private JRadioButton rdbtnNormal, rdbtnReal;
    private JTextArea textAreaResultados;
    private PanelGrafica panelGrafica;

    private JLabel lblGenActual, lblMejorFitness;
    private JSpinner spinPob, spinGens, spinCruce, spinMut, spinElite;
    private JComboBox<String> comboSeleccion, comboCruce;
    
    public VentanaPrincipal() {
        setTitle("Optimización de Cámaras - Panel de Control AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Ensanchamos a 1150 para que el panel de parámetros no colapse
        setBounds(50, 50, 1150, 800); 

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===== 1. BLOQUE CONFIGURACIÓN SUPERIOR =====
        JPanel pnlConfig = new JPanel();
        pnlConfig.setBounds(20, 10, 1090, 50);
        pnlConfig.setBorder(BorderFactory.createEtchedBorder());
        pnlConfig.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        contentPane.add(pnlConfig);

        comboEscenario = new JComboBox<>(new String[]{"Escenario 1 - Museo", "Escenario 2 - Pasillos", "Escenario 3 - Supermercado"});
        rdbtnNormal = new JRadioButton("Binario", true);
        rdbtnReal = new JRadioButton("Real");
        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rdbtnNormal); grupoModo.add(rdbtnReal);

        pnlConfig.add(new JLabel("Escenario:")); pnlConfig.add(comboEscenario);
        pnlConfig.add(Box.createHorizontalStrut(20));
        pnlConfig.add(new JLabel("Tipo:")); pnlConfig.add(rdbtnNormal); pnlConfig.add(rdbtnReal);
        pnlConfig.add(Box.createHorizontalStrut(20));
        chckbxPonderado = new JCheckBox("Ponderado");
        pnlConfig.add(chckbxPonderado);

        // ===== 2. BLOQUE PARÁMETROS AG (Optimizado para espacio) =====
        JPanel pnlParams = new JPanel();
        pnlParams.setBounds(20, 65, 1090, 65);
        pnlParams.setBorder(BorderFactory.createTitledBorder(null, "Configuración del Algoritmo", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 11)));
        pnlParams.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        contentPane.add(pnlParams);

        Dimension dimSpin = new Dimension(50, 22); // Spinners ligeramente más estrechos
        spinPob = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10)); spinPob.setPreferredSize(dimSpin);
        spinGens = new JSpinner(new SpinnerNumberModel(200, 1, 5000, 50)); spinGens.setPreferredSize(dimSpin);
        spinCruce = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5)); spinCruce.setPreferredSize(dimSpin);
        spinMut = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1)); spinMut.setPreferredSize(dimSpin);
        spinElite = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1)); spinElite.setPreferredSize(dimSpin);
        
        comboSeleccion = new JComboBox<>(new String[]{"Torneo", "Ruleta"});
        comboCruce = new JComboBox<>(new String[]{"Punto", "Aritm."}); // Nombres más cortos para ahorrar espacio

        pnlParams.add(new JLabel("Pob:")); pnlParams.add(spinPob);
        pnlParams.add(new JLabel("Gen:")); pnlParams.add(spinGens);
        pnlParams.add(new JLabel("Cr%:")); pnlParams.add(spinCruce);
        pnlParams.add(new JLabel("Mu%:")); pnlParams.add(spinMut);
        pnlParams.add(new JLabel("El%:")); pnlParams.add(spinElite);
        pnlParams.add(new JLabel("Sel:")); pnlParams.add(comboSeleccion);
        pnlParams.add(new JLabel("Cru:")); pnlParams.add(comboCruce);

        pnlParams.add(Box.createHorizontalStrut(10)); // Espacio antes del botón

        btnEjecutar = new JButton("EJECUTAR AG");
        btnEjecutar.setPreferredSize(new Dimension(130, 30));
        btnEjecutar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnEjecutar.setBackground(new Color(39, 174, 96)); // Un verde más elegante
        btnEjecutar.setForeground(Color.WHITE);
        btnEjecutar.setOpaque(true);
        btnEjecutar.setBorderPainted(false); // Opcional: quita el borde para un look más moderno
        btnEjecutar.setFocusPainted(false);  // Quita el recuadro de puntos al hacer clic
        // ----------------------------------------------------
        
        pnlParams.add(btnEjecutar);
        
     // 1. Añadimos en la sección de parámetros AG:
        comboSeleccion = new JComboBox<>(new String[]{"Torneo", "Ruleta", "Estocástico", "Truncamiento", "Restos"});
        pnlParams.add(new JLabel("Sel:")); 
        pnlParams.add(comboSeleccion);
        
        // ===== 3. MAPA ÚNICO Y RESULTADOS (Equilibrados) =====
        panelMapa = new PanelMapaReal();
        panelMapa.setBounds(20, 140, 520, 390); 
        panelMapa.setBorder(BorderFactory.createTitledBorder("Visualización"));
        contentPane.add(panelMapa);

        textAreaResultados = new JTextArea();
        textAreaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textAreaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(textAreaResultados);
        scroll.setBounds(555, 140, 555, 390); 
        scroll.setBorder(BorderFactory.createTitledBorder("Consola de Resultados"));
        contentPane.add(scroll);

        // ===== 4. ETIQUETAS ESTADÍSTICAS =====
        lblGenActual = new JLabel("Generación: 0");
        lblGenActual.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblGenActual.setBounds(30, 540, 200, 25);
        contentPane.add(lblGenActual);

        lblMejorFitness = new JLabel("Mejor Fitness: 0.00");
        lblMejorFitness.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblMejorFitness.setForeground(new Color(41, 128, 185));
        lblMejorFitness.setBounds(250, 540, 400, 25);
        contentPane.add(lblMejorFitness);

        // ===== 5. GRÁFICA =====
        panelGrafica = new PanelGrafica();
        panelGrafica.setBounds(20, 575, 1090, 170);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPane.add(panelGrafica);

        btnEjecutar.addActionListener(this::ejecutarAG);
        comboEscenario.addActionListener(e -> cargarEscenario(comboEscenario.getSelectedIndex()));

        cargarEscenario(0);
    }

    private void ejecutarAG(ActionEvent e) {

    	   new Thread(() -> {
               SwingUtilities.invokeLater(() -> {
                   panelGrafica.limpiar();
                   btnEjecutar.setEnabled(false);
                   textAreaResultados.setText("Ejecutando algoritmo...\n");
               });

               int escIdx = comboEscenario.getSelectedIndex();
               EscenarioDatos datos = EscenariosFactory.cargar(escIdx);
               Mapa mapaActual = (Mapa) datos.mapaObj;
               mapaActual.setMatrizImportancia(datos.importancia);

               int tPob = (int) spinPob.getValue();
               int tGen = (int) spinGens.getValue();
               double pCruce = (int) spinCruce.getValue() / 100.0;
               double pMut = (int) spinMut.getValue() / 100.0;
               double pElite = (int) spinElite.getValue() / 100.0;
               boolean ponderado = chckbxPonderado.isSelected();

               if (!rdbtnReal.isSelected()) {
            	    AlgoritmoGenetico ag = new AlgoritmoGenetico(mapaActual, datos.rango, datos.numCamaras, this);
            	    ag.setModoPonderado(ponderado);

            	    // Establecemos el método de selección según combo
            	    switch (comboSeleccion.getSelectedIndex()) {
            	        case 0: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.TORNEO); break;
            	        case 1: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.RULETA); break;
            	        case 2: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.ESTOCASTICO); break;
            	        case 3: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.TRUNCAMIENTO); break;
            	        case 4: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.RESTOS); break;
            	    }

            	    // Podemos elegir cruce uniforme/monopunto si quieres también desde otro combo
            	    ag.setMetodoCruce(AlgoritmoGenetico.MetodoCruce.MONOPUNTO); // por defecto
            	    Individuo mejor = ag.ejecutar(tGen, pMut, pCruce);
            	    SwingUtilities.invokeLater(() -> mostrarResultados(mejor));
            	}else {
                   AlgoritmoGenetico ag = new AlgoritmoGenetico(mapaActual, datos.rango, datos.numCamaras, this);
                   ag.setModoPonderado(ponderado);
                   Individuo mejor = ag.ejecutar(tGen, pMut,pCruce);
                   SwingUtilities.invokeLater(() -> mostrarResultados(mejor));
               }

               SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));
           }).start();
    }
    public void actualizarMapaRealEnTiempoReal(List<CamaraReal> cams, int g, double f, int r, double a) {
        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(cams, r, a);
            panelMapa.repaint();
            lblGenActual.setText("Generación: " + g);
            lblMejorFitness.setText("Mejor Fitness: " + String.format("%.2f", f));
        });
    }

    public void actualizarMapaEnTiempoReal(Individuo ind, int gen) {
        SwingUtilities.invokeLater(() -> {
            List<CamaraReal> visual = new ArrayList<>();
            for (Camara c : ind.camaras) visual.add(new CamaraReal(c.x, c.y, 0));
            panelMapa.setCamaras(visual, 1, 0); 
            panelMapa.repaint();
            lblGenActual.setText("Generación: " + gen);
            lblMejorFitness.setText("Mejor Fitness: " + String.format("%.2f", ind.fitness));
        });
    }

    public void actualizarGrafica(double mGen, double mAbs, double med) {
        SwingUtilities.invokeLater(() -> panelGrafica.agregarDatos(mGen, mAbs, med));
    }

    private void cargarEscenario(int idx) {
        EscenarioDatos d = EscenariosFactory.cargar(idx);
        panelMapa.setMapa(d.mapa, d.importancia);
        panelMapa.repaint();
        String nombreEsc = comboEscenario.getSelectedItem().toString();
        textAreaResultados.setText("Escenario: " + nombreEsc + "\n");
    }

    private void mostrarResultadosReal(IndividuoReal ind, int r, double a) {
        textAreaResultados.setText("=== RESULTADOS FINALES (REAL) ===\n");
        textAreaResultados.append("Fitness Máximo: " + String.format("%.2f", ind.fitness) + "\n\n");
        for(int i=0; i<ind.camaras.size(); i++) {
            CamaraReal c = ind.camaras.get(i);
            textAreaResultados.append(String.format("Cámara %d: X:%.1f, Y:%.1f, Áng:%.1f°\n", i+1, c.x, c.y, c.theta));
        }
        panelMapa.setCamaras(ind.camaras, r, a);
        panelMapa.repaint();
    }

    private void mostrarResultados(Individuo ind) {
        textAreaResultados.setText("=== RESULTADOS FINALES (BINARIO) ===\n");
        textAreaResultados.append("Fitness Máximo: " + String.format("%.2f", ind.fitness) + "\n\n");
        List<CamaraReal> lista = new ArrayList<>();
        for(Camara c : ind.camaras) {
            lista.add(new CamaraReal(c.x, c.y, 0));
            textAreaResultados.append(String.format("Cámara en: (%d, %d)\n", (int)c.x, (int)c.y));
        }
        panelMapa.setCamaras(lista, 1, 0);
        panelMapa.repaint();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        EventQueue.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }    
}