/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Graficos;

import Componentes.PCB;
import Estructuras.Cola;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author luismarianolovera
 */
public class GraficarSimulacion {
    public static void graficaTurnaround(Cola<PCB> terminados) {

        // Dataset de barras
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Cola<PCB> backup = new Cola<PCB>();

        while (!terminados.esVacia()) {
            PCB p = terminados.desencolar();
            backup.encolar(p);

            // turno = fin - llegada
            Integer ttat = p.getTurnaround();
            if (ttat == null) {
                ttat = 0;
            }
          
            // addValue(valor, fila/serie, columna/categoría)
            dataset.addValue(ttat,p.getNombre(),"Turnaround");
        }

        // Restaurar la cola tal como estaba
        while (!backup.esVacia()) {
            terminados.encolar(backup.desencolar());
        }

        // Creacion de gráfico de barras
        JFreeChart grafico = ChartFactory.createBarChart3D(
            "Turnaround por Proceso",
            "Proceso",
            "Ciclos de Turnaround",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        mostrarEnVentana(grafico, "Turnaround por Proceso");
    }

    /**
     * Grafica el uso de CPU en el tiempo.
     * histTiempo[i] = ciclo del Kernel
     * histUsoCPU[i] = 0 o 100
     * n = cantidad válida de puntos
     */
    public static void graficaUsoCPU(int[] histTiempo, int[] histUsoCPU, int n) {

        XYSeries serie = new XYSeries("Uso CPU (%)");

        int i = 0;
        while (i < n) {
            // x = ciclo, y = %
            serie.add(histTiempo[i], histUsoCPU[i]);
            i = i + 1;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serie);

        JFreeChart grafico = ChartFactory.createXYLineChart(
            "Uso de CPU en el tiempo",
            "Ciclo",
            "Uso (%)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        mostrarEnVentana(grafico, "Uso de CPU (%)");
    }

    /**
     * Helper común: abre un JFrame con el gráfico adentro.
     */
    private static void mostrarEnVentana(JFreeChart grafico, String tituloVentana) {
        ChartPanel panel = new ChartPanel(grafico);

        JFrame frame = new JFrame(tituloVentana);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // centrar
        frame.setVisible(true);
    }
}
    

