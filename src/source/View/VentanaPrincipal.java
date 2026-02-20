package source.View;

import javax.swing.*;
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
    private PanelMapaReal panelMapaMejor;
    private JButton btnEjecutar;
    private JCheckBox chckbxPonderado;
    private JComboBox<String> comboEscenario;
    private JRadioButton rdbtnNormal, rdbtnReal;
    private JTextArea textAreaResultados;
    private JSpinner spinnerGeneraciones;
    private PanelGrafica panelGrafica;
    
    private IndividuoReal mejorAbsolutoReal = null;
    private Individuo mejorAbsoluto = null;
    private double mejorFitnessAbsoluto = -1.0; // Cambiado a double para compatibilidad

    public VentanaPrincipal() {
        setTitle("Optimización de Cámaras - AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 650);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===== CONFIGURACIÓN UI =====
        comboEscenario = new JComboBox<>(new String[]{
                "Escenario 1 - Museo",
                "Escenario 2 - Pasillos",
                "Escenario 3 - Supermercado"
        });
        comboEscenario.setBounds(20, 15, 220, 25);
        contentPane.add(comboEscenario);

        chckbxPonderado = new JCheckBox("Modo ponderado");
        chckbxPonderado.setBounds(260, 15, 150, 25);
        contentPane.add(chckbxPonderado);

        rdbtnNormal = new JRadioButton("Normal");
        rdbtnNormal.setBounds(420, 15, 80, 25);
        rdbtnNormal.setSelected(true);
        rdbtnReal = new JRadioButton("Real");
        rdbtnReal.setBounds(510, 15, 80, 25);

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rdbtnNormal);
        grupoModo.add(rdbtnReal);
        contentPane.add(rdbtnNormal);
        contentPane.add(rdbtnReal);

        JLabel labelGeneraciones = new JLabel("Gens:");
        labelGeneraciones.setBounds(880, 15, 50, 25);
        contentPane.add(labelGeneraciones);

        spinnerGeneraciones = new JSpinner(new SpinnerNumberModel(500, 1, 5000, 50));
        spinnerGeneraciones.setBounds(930, 15, 70, 25);
        contentPane.add(spinnerGeneraciones);
        
        btnEjecutar = new JButton("Ejecutar AG");
        btnEjecutar.setBounds(610, 15, 150, 25);
        contentPane.add(btnEjecutar);

        // ===== PANELES DE MAPA =====
        panelMapa = new PanelMapaReal();
        panelMapa.setBounds(20, 60, 350, 350);
        panelMapa.setBorder(BorderFactory.createTitledBorder("Evolución en tiempo real"));
        contentPane.add(panelMapa);

        panelMapaMejor = new PanelMapaReal();
        panelMapaMejor.setBounds(760, 60, 350, 350);
        panelMapaMejor.setBorder(BorderFactory.createTitledBorder("Mejor solución histórica"));
        contentPane.add(panelMapaMejor);
        
        panelGrafica = new PanelGrafica();
        panelGrafica.setBounds(20, 420, 1100, 150);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        contentPane.add(panelGrafica);

        textAreaResultados = new JTextArea();
        textAreaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(textAreaResultados);
        scroll.setBounds(390, 60, 350, 350);
        contentPane.add(scroll);

        btnEjecutar.addActionListener(this::ejecutarAG);
        cargarEscenario(0);
    }

    private void ejecutarAG(ActionEvent e) {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                panelGrafica.limpiar();
                btnEjecutar.setEnabled(false);
                mejorFitnessAbsoluto = -1.0;
                mejorAbsoluto = null;
                mejorAbsolutoReal = null;
            });

            int escenarioIdx = comboEscenario.getSelectedIndex();
            EscenarioDatos datos = EscenariosFactory.cargar(escenarioIdx);
            
            // === PASO 3: Vincular importancia al objeto Mapa ===
            Mapa mapaActual = (Mapa) datos.mapaObj;
            mapaActual.setMatrizImportancia(datos.importancia); 
            // ===================================================

            boolean ponderado = chckbxPonderado.isSelected();
            int numGens = (int) spinnerGeneraciones.getValue();

            if (rdbtnReal.isSelected()) {
                ejecutarAGReal(datos, ponderado, numGens);
            } else {
                AlgoritmoGenetico ag = new AlgoritmoGenetico(
                        mapaActual, 
                        datos.rango, 
                        datos.numCamaras, 
                        this
                );
                
                ag.setModoPonderado(ponderado);
                // Ya no necesitas ag.setImportancia porque el AG 
                // ahora lee directamente del mapaActual
                
                Individuo candidato = ag.ejecutar(numGens, 0.15);
                SwingUtilities.invokeLater(() -> mostrarResultados(candidato));
            }

            SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));
        }).start();
    }

    private void ejecutarAGReal(EscenarioDatos datos, boolean ponderado, int generaciones) {
        AlgoritmoGeneticoReal agReal = new AlgoritmoGeneticoReal(
                (Mapa) datos.mapaObj,
                datos.rango,
                datos.numCamaras,
                60.0,
                this
        );
        agReal.setModoPonderado(ponderado);
        agReal.setImportancia(datos.importancia);

        IndividuoReal candidato = agReal.ejecutar(generaciones, 0.15);
        SwingUtilities.invokeLater(() -> mostrarResultadosReal(candidato, datos.rango, 60.0));
    }

    public void actualizarMapaEnTiempoReal(Individuo ind, int gen) {
        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(convertirACamarasReales(ind.camaras), 1, 0);
            panelMapa.repaint();
            setTitle("Generación: " + gen + " | Fitness: " + String.format("%.2f", ind.fitness));
        });
    }

    public void actualizarMapaRealEnTiempoReal(List<CamaraReal> camaras, int gen, double fitness, int rango, double apertura) {
        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(camaras, rango, apertura);
            panelMapa.repaint();
            setTitle("Generación: " + gen + " | Fitness: " + String.format("%.2f", fitness));
        });
    }

    private void mostrarResultados(Individuo candidato) {
        if (mejorAbsoluto == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;
            mejorAbsoluto = copiarIndividuo(candidato);

            textAreaResultados.setText("=== MEJOR SOLUCIÓN NORMAL ===\n");
            textAreaResultados.append(String.format("Fitness: %.2f\n\n", mejorFitnessAbsoluto));

            for (Camara c : mejorAbsoluto.camaras) {
                textAreaResultados.append(String.format("Cámara en (%d, %d)\n", (int)c.x, (int)c.y));
            }

            panelMapaMejor.setCamaras(convertirACamarasReales(mejorAbsoluto.camaras), 1, 0);
            panelMapaMejor.repaint();
        }
    }

    private void mostrarResultadosReal(IndividuoReal candidato, int rango, double apertura) {
        if (mejorAbsolutoReal == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;
            mejorAbsolutoReal = candidato;

            textAreaResultados.setText("=== MEJOR SOLUCIÓN REAL ===\n");
            textAreaResultados.append(String.format("Fitness: %.4f\n\n", candidato.fitness));

            for (int i = 0; i < candidato.camaras.size(); i++) {
                CamaraReal c = candidato.camaras.get(i);
                textAreaResultados.append(String.format("Cámara %d: (%.2f, %.2f) θ: %.1f°\n", 
                                          (i+1), c.x, c.y, c.theta));
            }

            panelMapaMejor.setCamaras(candidato.camaras, rango, apertura);
            panelMapaMejor.repaint();
        }
    }

    private void cargarEscenario(int idx) {
        EscenarioDatos d = EscenariosFactory.cargar(idx);
        panelMapa.setMapa(d.mapa, d.importancia);
        panelMapaMejor.setMapa(d.mapa, d.importancia);
    }

    private List<CamaraReal> convertirACamarasReales(List<Camara> normales) {
        List<CamaraReal> lista = new ArrayList<>();
        for (Camara c : normales) {
            lista.add(new CamaraReal(c.x, c.y, 0)); 
        }
        return lista;
    }

    private Individuo copiarIndividuo(Individuo ind) {
        Individuo copia = new Individuo();
        for (Camara c : ind.camaras) {
            copia.camaras.add(new Camara(c.x, c.y));
        }
        copia.fitness = ind.fitness;
        return copia;
    }

    public void actualizarGrafica(double mejorGen, double mejorAbs, double media) {
        SwingUtilities.invokeLater(() -> panelGrafica.agregarDatos(mejorGen, mejorAbs, media));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}