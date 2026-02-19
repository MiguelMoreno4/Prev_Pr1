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
import source.View.*;

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
    private double mejorFitnessAbsoluto = -1;

    public VentanaPrincipal() {

        setTitle("Optimización de Cámaras - AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 650);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===== ESCENARIO =====
        comboEscenario = new JComboBox<>(new String[]{
                "Escenario 1 - Museo",
                "Escenario 2 - Pasillos",
                "Escenario 3 - Supermercado"
        });
        comboEscenario.setBounds(20, 15, 220, 25);
        contentPane.add(comboEscenario);

        // ===== PONDERADO =====
        chckbxPonderado = new JCheckBox("Modo ponderado");
        chckbxPonderado.setBounds(260, 15, 150, 25);
        contentPane.add(chckbxPonderado);

        // ===== MODO NORMAL/REAL =====
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
        
        // JSpinner numGeneraciones
        JLabel labelGeneraciones = new JLabel("Generaicones: ");
        labelGeneraciones.setBounds(900, 15, 40, 25);
        contentPane.add(labelGeneraciones);
        // Modelo: valor inicial 500, mínimo 1, máximo 5000, paso de 50
        SpinnerNumberModel modeloGens = new SpinnerNumberModel(500, 1, 5000, 50);
        spinnerGeneraciones = new JSpinner(modeloGens);
        spinnerGeneraciones.setBounds(940, 15, 70, 25);
        contentPane.add(spinnerGeneraciones);
        
        // ===== BOTÓN EJECUTAR =====
        btnEjecutar = new JButton("Ejecutar AG");
        btnEjecutar.setBounds(610, 15, 150, 25);
        contentPane.add(btnEjecutar);

        // ===== MAPA =====
        panelMapa = new PanelMapaReal();
        panelMapa.setBounds(20, 60, 350, 350);
        panelMapa.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        contentPane.add(panelMapa);
     // ===== MAPA MEJOR SOLUCIÓN =====
        panelMapaMejor = new PanelMapaReal();
        panelMapaMejor.setBounds(760, 60, 350, 350);
        panelMapaMejor.setBorder(BorderFactory.createTitledBorder("Mejor solución"));
        contentPane.add(panelMapaMejor);
        
        //Grafico
        panelGrafica = new PanelGrafica();
        panelGrafica.setBounds(20, 420, 760, 120);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        contentPane.add(panelGrafica);

        // ===== RESULTADOS =====
        textAreaResultados = new JTextArea();
        textAreaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(textAreaResultados);
        scroll.setBounds(390, 60, 350, 350);
        contentPane.add(scroll);

        btnEjecutar.addActionListener(this::ejecutarAG);

        cargarEscenario(0);
    }

    /**
     * Función común para ejecutar AG. 
     * Decide si usar modo normal o real según el radioButton.
     */
    private void ejecutarAG(ActionEvent e) {
    	
    	new Thread(() -> {
    		 panelGrafica.limpiar();
            btnEjecutar.setEnabled(false); // Desactivar botón mientras corre

            int escenario = comboEscenario.getSelectedIndex();
            EscenarioDatos datos = EscenariosFactory.cargar(escenario);
            panelMapa.setMapa(datos.mapa, datos.importancia);

            boolean ponderado = chckbxPonderado.isSelected();
            int numGens = (int) spinnerGeneraciones.getValue();

            if (rdbtnReal.isSelected()) {
                ejecutarAGReal(datos, ponderado, numGens);
            } else {
            	// Usamos datos.mapaObj para obtener la matriz de obstáculos real
                // y pasamos 'this' para la animación
            	AlgoritmoGenetico ag = new AlgoritmoGenetico(
                        (Mapa) datos.mapaObj, 
                        datos.rango, 
                        datos.numCamaras, 
                        this
                );
            	
                ag.setModoPonderado(ponderado);
                if (ponderado) ag.setImportancia(datos.importancia);
                
                Individuo candidato = ag.ejecutar(numGens, 0.15);
                mostrarResultados(candidato);
            }

            btnEjecutar.setEnabled(true); // Reactivar al terminar
        }).start();
    	
    	/*
    	this.mejorAbsoluto = null;
    	this.mejorFitnessAbsoluto = -1;
    	int numGens = (int) spinnerGeneraciones.getValue();
    	
        int escenario = comboEscenario.getSelectedIndex();
        EscenarioDatos datos = EscenariosFactory.cargar(escenario);

        panelMapa.setMapa(datos.mapa, datos.importancia);

        boolean ponderado = chckbxPonderado.isSelected();
        boolean modoReal = rdbtnReal.isSelected();

        if (modoReal) {
            ejecutarAGReal(datos, ponderado, numGens);
        } else {
            ejecutarAGNormal(datos, ponderado, numGens);
        }
        */
    }

    /**
     * Ejecución del AG normal (V1)
     */
    private void ejecutarAGNormal(EscenarioDatos datos, boolean ponderado, int generaciones) {
        AlgoritmoGenetico ag = new AlgoritmoGenetico(
                datos.mapaObj,
                datos.rango,
                datos.numCamaras,
                this
        );
        ag.setModoPonderado(ponderado);
        if (ponderado) ag.setImportancia(datos.importancia);

        Individuo candidato = ag.ejecutar(generaciones, 0.15);
        // PAra asegurar que los resultados finales se impriman en el textArea
        SwingUtilities.invokeLater(() -> {
            mostrarResultados(candidato);
        });    }

    /**
     * Ejecución del AG real (V2 con cámaras orientables)
     * Aquí se llama a tu AlgoritmoGeneticoReal
     */
    private void ejecutarAGReal(EscenarioDatos datos, boolean ponderado, int generaciones) {
    	Mapa mapaActual = (Mapa) datos.mapaObj;

        AlgoritmoGeneticoReal agReal = new AlgoritmoGeneticoReal(
                mapaActual,       // Usamos el mapa cargado, no uno fijo
                datos.rango,
                datos.numCamaras,
                60.0,             // Apertura
                this              // Pasamos la ventana para la animación
        );
        agReal.setModoPonderado(ponderado);
        if (ponderado) agReal.setImportancia(datos.importancia);

        IndividuoReal candidato = agReal.ejecutar(generaciones, 0.15);
        mostrarResultadosReal(candidato, datos.rango, 60.0);
    }

    /*
    	Actualizar el mapa para ir mostrando como cambian las camaras
    */
    public void actualizarMapaEnTiempoReal(Individuo ind, int gen) {
        SwingUtilities.invokeLater(() -> {
        	panelMapa.setCamaras(convertirACamarasReales(ind.camaras), 1, 0);
        	panelMapa.repaint();
            // Opcional: mostrar progreso en el título o un label
            setTitle("Generación: " + gen + " | Mejor Fitness: " + ind.fitness);
        });
    }
    
    private List<CamaraReal> convertirACamarasReales(List<Camara> normales) {
        List<CamaraReal> lista = new java.util.ArrayList<>();
        for (Camara c : normales) {
            lista.add(new CamaraReal(c.x, c.y, 0)); // θ=0 y apertura será 360
        }
        return lista;
    }

	public void actualizarMapaRealEnTiempoReal(List<CamaraReal> camaras, int gen, double fitness, int rango, double apertura) {
        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(camaras, rango, apertura);
            setTitle("Generación: " + gen + " | Fitness: " + fitness);
        });
    }
    
    /**
     * Mostrar resultados del AG normal
     */
    private void mostrarResultados(Individuo candidato) {
        if (mejorAbsoluto == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;
            mejorAbsoluto = copiarIndividuo(candidato);

            textAreaResultados.setText("");
            textAreaResultados.append("MEJOR SOLUCIÓN\n");
            textAreaResultados.append("Fitness = " + mejorFitnessAbsoluto + "\n\n");

            for (Camara c : mejorAbsoluto.camaras) {
                textAreaResultados.append("Cámara en (" + c.x + "," + c.y + ")\n");
            }

            //panelMapa.setCamaras(convertirACamarasReales(mejorAbsoluto.camaras), 1, 0);
            panelMapaMejor.setCamaras(
            	    convertirACamarasReales(mejorAbsoluto.camaras),
            	    1,
            	    0
            	);
            	panelMapaMejor.repaint();
        }
    }

    /**
     * Mostrar resultados del AG real
     * 
    private void mostrarResultadosReal(IndividuoReal candidato) {
        if (mejorAbsoluto == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;

            // Convertir IndividuoReal a camaras redondeadas para el panel
            mejorAbsoluto = new Individuo();
            for (CamaraReal c : candidato.camaras) {
                mejorAbsoluto.camaras.add(new Camara(
                        (int)Math.round(c.x),
                        (int)Math.round(c.y)
                ));
            }
            mejorAbsoluto.fitness = candidato.fitness;

            textAreaResultados.setText("");
            textAreaResultados.append("MEJOR SOLUCIÓN (Real)\n");
            textAreaResultados.append("Fitness = " + mejorFitnessAbsoluto + "\n\n");

            for (CamaraReal c : candidato.camaras) {
                textAreaResultados.append(String.format(
                        "Cámara en (%.2f, %.2f), θ=%.1f°\n",
                        c.x, c.y, c.theta
                ));
            }

            panelMapa.setCamaras(mejorAbsoluto.camaras);
        }
    }
     */
    private void mostrarResultadosReal(IndividuoReal candidato, int rango, double apertura) {
        if (mejorAbsolutoReal == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;
            mejorAbsolutoReal = candidato; // Guardamos el objeto REAL, no el redondeado

            textAreaResultados.setText("");
            textAreaResultados.append("=== MEJOR SOLUCIÓN REAL ===\n");
            textAreaResultados.append(String.format("Fitness: %.4f\n\n", candidato.fitness));

            for (int i = 0; i < candidato.camaras.size(); i++) {
                CamaraReal c = candidato.camaras.get(i);
                textAreaResultados.append(String.format("Cámara %d: (%.2f, %.2f) θ: %.1f°\n", 
                                          (i+1), c.x, c.y, c.theta));
            }

            // Actualizamos el panel con los datos EXACTOS
           // panelMapa.setCamaras(candidato.camaras, rango, apertura);
            panelMapaMejor.setCamaras(candidato.camaras, rango, apertura);
            panelMapaMejor.repaint();
        }
    }
    private void cargarEscenario(int idx) {
        EscenarioDatos d = EscenariosFactory.cargar(idx);
        panelMapa.setMapa(d.mapa, d.importancia);
        panelMapaMejor.setMapa(d.mapa, d.importancia);

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
        SwingUtilities.invokeLater(() -> {
            panelGrafica.agregarDatos(mejorGen, mejorAbs, media);
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}