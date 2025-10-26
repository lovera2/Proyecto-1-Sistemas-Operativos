/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author luismarianolovera
 */
public class Interfaz extends javax.swing.JFrame {
    
    private static final Logger logger = Logger.getLogger(Interfaz.class.getName());
    
    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        initComponents();
        CPUproceso.setVisible(false);
        CPUid1.setVisible(false);
        CPUpc.setVisible(false);
        CPUmar.setVisible(false);
        CPUestado.setVisible(false);

        // poner valores iniciales en los labels visibles
        jLabelProcesoValor.setText("-");
        jLabelIDValor.setText("-");
        jLabelPCValor.setText("-");
        jLabelMARValor.setText("-");
        jLabelEstadoValor.setText("-");

        jLabelRelojGlobal.setText("-");
        jLabelPoliticaActual.setText("-");

        // barra de uso de CPU en 0%
        barraUsoCPU.setValue(0);
        barraUsoCPU.setString("0%");

        // dejar log vacío
        areaLog.setText("");
    }
        
    public void actualizarCPU(
        String nombreProceso,
        String pid,
        String pc,
        String mar,
        String estado,
        int usoCPU
    ) {
        jLabelProcesoValor.setText(nombreProceso != null ? nombreProceso : "-");
        jLabelIDValor.setText(pid != null ? pid : "-");
        jLabelPCValor.setText(pc != null ? pc : "-"); // si renombras este a jLabelPCValor, actualiza aquí
        jLabelMARValor.setText(mar != null ? mar : "-");
        jLabelEstadoValor.setText(estado != null ? estado : "-");

        if (usoCPU < 0) usoCPU = 0;
        if (usoCPU > 100) usoCPU = 100;
        barraUsoCPU.setValue(usoCPU);
        barraUsoCPU.setString(usoCPU + "%");
    }

    /**
     * Actualiza el panel "Estado global":
     * - ciclo del reloj
     * - política actual
     */
    public void actualizarEstadoGlobal(String cicloTexto, String politicaTexto) {
        jLabelRelojGlobal.setText(
                (cicloTexto != null && !cicloTexto.isEmpty()) ? cicloTexto : "-"
        );
        jLabelPoliticaActual.setText(
                (politicaTexto != null && !politicaTexto.isEmpty()) ? politicaTexto : "-"
        );
    }

    /**
     * Agrega una línea al log del sistema (panel Log del sistema).
     * Auto-scroll al final.
     */
    public void logEvento(String linea) {
        if (linea == null) return;
        areaLog.append(linea + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    /**
     * Rellena una tabla (por ejemplo tablaListos, tablaBloqueados, etc.)
     * con datos nuevos.
     *
     * Uso típico:
     *
     *   Object[][] filas = {
     *       {1, "P1", "READY", 120, 400, 5},
     *       {2, "P2", "RUNNING", 200, 500, 3}
     *   };
     *   refrescarTabla(tablaListos, filas);
     *
     * Esto NO usa ArrayList, solo un arreglo bidimensional.
     * Más adelante puedes construir ese arreglo recorriendo tu Cola enlazada.
     */
    public void refrescarTabla(JTable tabla, Object[][] filas) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();

        // borrar filas viejas
        int rowCount = model.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            model.removeRow(i);
        }

        // cargar filas nuevas
        if (filas != null) {
            for (Object[] fila : filas) {
                model.addRow(fila);
            }
        }
    }
    
    public JTable getTablaListos() {
        return tablaListos;
    }

    public JTable getTablaBloqueados() {
        return tablaBloqueados;
    }

    public JTable getTablaListosSuspendidos() {
        return tablaListosSuspendidos;
    }

    public JTable getTablaBloqueadosSuspendidos() {
        return tablaBloqueadosSuspendidos;
    }

    public JTable getTablaTerminados() {
        return tablaTerminados;
    }

    /**
     * Ejemplo de cómo podrías, más adelante,
     * refrescar específicamente la cola de listos usando tu propia estructura.
     *
     * Esto es SOLO una guía, está comentado para que no rompa compile
     * hasta que conectes tus clases reales.
     *
     *  public void refrescarColaListos(Estructuras.Cola colaListos) {
     *      DefaultTableModel model =
     *          (DefaultTableModel) tablaListos.getModel();
     *
     *      // limpiar
     *      int rowCount = model.getRowCount();
     *      for (int i = rowCount - 1; i >= 0; i--) {
     *          model.removeRow(i);
     *      }
     *
     *      // recorrer tu cola enlazada
     *      Estructuras.Nodo actual = colaListos.getFrente();
     *      while (actual != null) {
     *          Componentes.PCB p = actual.getProceso(); // ajusta a tu getter
     *          Object[] fila = new Object[] {
     *              p.getPid(),
     *              p.getNombre(),
     *              p.getEstado(),
     *              p.getPC(),
     *              p.getMAR(),
     *              p.getQRestante()
     *          };
     *          model.addRow(fila);
     *          actual = actual.getSiguiente();
     *      }
     *  }
     */
            

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupoTipoProceso = new javax.swing.ButtonGroup();
        pestanas = new javax.swing.JTabbedPane();
        panelConfiguracion = new javax.swing.JPanel();
        panelGraficos = new javax.swing.JPanel();
        panelEstadisticas = new javax.swing.JPanel();
        panelSimulador = new javax.swing.JPanel();
        panelCPU = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        CPUproceso = new javax.swing.JLabel();
        CPUpc = new javax.swing.JLabel();
        CPUid1 = new javax.swing.JLabel();
        CPUmar = new javax.swing.JLabel();
        CPUestado = new javax.swing.JLabel();
        jLabelProcesoValor = new javax.swing.JLabel();
        jLabelIDValor = new javax.swing.JLabel();
        jLabelPCValor = new javax.swing.JLabel();
        jLabelEstadoValor = new javax.swing.JLabel();
        jLabelMARValor = new javax.swing.JLabel();
        barraUsoCPU = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        btnIniciar = new javax.swing.JButton();
        btnPausar = new javax.swing.JButton();
        btnReiniciar = new javax.swing.JButton();
        btnVerGraficas = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablaListos = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaBloqueados = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        scrollLog = new javax.swing.JScrollPane();
        tablaListosSuspendidos = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tablaBloqueadosSuspendidos = new javax.swing.JTable();
        jScrollPane9 = new javax.swing.JScrollPane();
        tablaTerminados = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        panelEstadoGlobal = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        comboPolitica = new javax.swing.JComboBox<>();
        aplicarPolitica = new javax.swing.JButton();
        jLabelRelojGlobal = new javax.swing.JLabel();
        jLabelPoliticaActual = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        areaLog = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txtNombreProc = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        spInstrucciones = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        rbCPUbound = new javax.swing.JRadioButton();
        rbIObound = new javax.swing.JRadioButton();
        jLabel22 = new javax.swing.JLabel();
        spIOCadaN = new javax.swing.JSpinner();
        btnCrearProceso = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout panelConfiguracionLayout = new javax.swing.GroupLayout(panelConfiguracion);
        panelConfiguracion.setLayout(panelConfiguracionLayout);
        panelConfiguracionLayout.setHorizontalGroup(
            panelConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1462, Short.MAX_VALUE)
        );
        panelConfiguracionLayout.setVerticalGroup(
            panelConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
        );

        pestanas.addTab("Configuración", panelConfiguracion);

        javax.swing.GroupLayout panelGraficosLayout = new javax.swing.GroupLayout(panelGraficos);
        panelGraficos.setLayout(panelGraficosLayout);
        panelGraficosLayout.setHorizontalGroup(
            panelGraficosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1462, Short.MAX_VALUE)
        );
        panelGraficosLayout.setVerticalGroup(
            panelGraficosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
        );

        pestanas.addTab("Gráficos", panelGraficos);

        javax.swing.GroupLayout panelEstadisticasLayout = new javax.swing.GroupLayout(panelEstadisticas);
        panelEstadisticas.setLayout(panelEstadisticasLayout);
        panelEstadisticasLayout.setHorizontalGroup(
            panelEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1462, Short.MAX_VALUE)
        );
        panelEstadisticasLayout.setVerticalGroup(
            panelEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
        );

        pestanas.addTab("Estadísticas", panelEstadisticas);

        panelCPU.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("CPU actual");

        jLabel2.setText("Proceso:");

        jLabel3.setText("ID: ");

        jLabel4.setText("PC:");

        jLabel5.setText("MAR:");

        jLabel6.setText("Estado:");

        jLabelProcesoValor.setText("-");

        jLabelIDValor.setText("-");

        jLabelPCValor.setText("-");

        jLabelEstadoValor.setText("-");

        jLabelMARValor.setText("-");

        barraUsoCPU.setStringPainted(true);

        javax.swing.GroupLayout panelCPULayout = new javax.swing.GroupLayout(panelCPU);
        panelCPU.setLayout(panelCPULayout);
        panelCPULayout.setHorizontalGroup(
            panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCPULayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panelCPULayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(barraUsoCPU, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCPULayout.createSequentialGroup()
                            .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5)
                                .addComponent(jLabel6))
                            .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelCPULayout.createSequentialGroup()
                                    .addGap(24, 24, 24)
                                    .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabelIDValor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelPCValor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCPULayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelMARValor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelEstadoValor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGroup(panelCPULayout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addGap(18, 18, 18)
                            .addComponent(jLabelProcesoValor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CPUproceso, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CPUid1)
                    .addComponent(CPUpc)
                    .addComponent(CPUmar)
                    .addComponent(CPUestado))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCPULayout.setVerticalGroup(
            panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCPULayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(CPUproceso)
                    .addComponent(jLabelProcesoValor))
                .addGap(18, 18, 18)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(CPUid1)
                    .addComponent(jLabelIDValor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(CPUpc)
                    .addComponent(jLabelPCValor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(CPUmar)
                    .addComponent(jLabelMARValor))
                .addGap(18, 18, 18)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(CPUestado)
                    .addComponent(jLabelEstadoValor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(barraUsoCPU, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setText("Controles");

        btnIniciar.setText("Iniciar");

        btnPausar.setText("Pausar");

        btnReiniciar.setText("Reiniciar");

        btnVerGraficas.setText("Ver Graficas");
        btnVerGraficas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerGraficasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnVerGraficas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnReiniciar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPausar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnIniciar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(jLabel7)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnIniciar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPausar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReiniciar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVerGraficas)
                .addContainerGap(95, Short.MAX_VALUE))
        );

        jLabel8.setText("Cola Listos");

        tablaListos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Estado", "PC", "MAR", "QRestante"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tablaListos);

        jLabel9.setText("Cola Bloqueados");

        tablaBloqueados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Estado", "PC", "MAR", "QRestante"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tablaBloqueados);

        jLabel10.setText("Cola Listos Suspendidos");

        tablaListosSuspendidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Estado", "PC", "MAR", "QRestante"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollLog.setViewportView(tablaListosSuspendidos);

        jLabel11.setText("Cola Bloqueados Suspendidos");

        tablaBloqueadosSuspendidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Estado", "PC", "MAR", "QRestante"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane8.setViewportView(tablaBloqueadosSuspendidos);

        tablaTerminados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Estado", "PC", "MAR", "QRestante"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane9.setViewportView(tablaTerminados);

        jLabel12.setText("Cola Terminados");

        panelEstadoGlobal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel13.setText("Estado global");

        jLabel14.setText("Ciclo de reloj:");

        jLabel15.setText("Politica actual:");

        jLabel16.setText("Cambiar política:");

        comboPolitica.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "SPN", "SRT", "HRRN", "Feedback" }));

        aplicarPolitica.setText("Aplicar política");

        jLabelRelojGlobal.setText("-");

        jLabelPoliticaActual.setText("-");

        javax.swing.GroupLayout panelEstadoGlobalLayout = new javax.swing.GroupLayout(panelEstadoGlobal);
        panelEstadoGlobal.setLayout(panelEstadoGlobalLayout);
        panelEstadoGlobalLayout.setHorizontalGroup(
            panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aplicarPolitica)
                            .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel14))
                                .addGap(18, 18, 18)
                                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelRelojGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabelPoliticaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel16)))
                    .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(jLabel13)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        panelEstadoGlobalLayout.setVerticalGroup(
            panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabelRelojGlobal))
                .addGap(18, 18, 18)
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabelPoliticaActual))
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aplicarPolitica)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel17.setText("Log del sistema");

        areaLog.setEditable(false);
        areaLog.setColumns(20);
        areaLog.setRows(5);
        jScrollPane1.setViewportView(areaLog);

        jScrollPane2.setViewportView(jScrollPane1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(jLabel17))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel18.setText("Crear proceso");

        jLabel19.setText("Tipo:");

        jLabel20.setText("Nombre:");

        spInstrucciones.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));

        jLabel21.setText("Instrucciones totales:");

        grupoTipoProceso.add(rbCPUbound);
        rbCPUbound.setText("CPU-bound");

        grupoTipoProceso.add(rbIObound);
        rbIObound.setText("I/O-bound");
        rbIObound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbIOboundActionPerformed(evt);
            }
        });

        jLabel22.setText("I/O cada N ciclos:");

        spIOCadaN.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));

        btnCrearProceso.setText("Crear proceso");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addGap(97, 97, 97))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spIOCadaN, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(rbCPUbound)
                        .addGap(18, 18, 18)
                        .addComponent(rbIObound))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtNombreProc)
                        .addComponent(jLabel19)
                        .addComponent(jLabel20)
                        .addComponent(spInstrucciones, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                    .addComponent(btnCrearProceso, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18)
                .addGap(7, 7, 7)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNombreProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spInstrucciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCPUbound)
                    .addComponent(rbIObound))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spIOCadaN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCrearProceso)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelSimuladorLayout = new javax.swing.GroupLayout(panelSimulador);
        panelSimulador.setLayout(panelSimuladorLayout);
        panelSimuladorLayout.setHorizontalGroup(
            panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSimuladorLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCPU, javax.swing.GroupLayout.PREFERRED_SIZE, 208, Short.MAX_VALUE))
                .addGap(46, 46, 46)
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel12)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                    .addComponent(scrollLog)
                    .addComponent(jScrollPane6)
                    .addComponent(jScrollPane5)
                    .addComponent(jScrollPane9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(panelEstadoGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(237, 237, 237))
        );
        panelSimuladorLayout.setVerticalGroup(
            panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSimuladorLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSimuladorLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(scrollLog, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelSimuladorLayout.createSequentialGroup()
                        .addComponent(panelCPU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSimuladorLayout.createSequentialGroup()
                        .addGap(0, 11, Short.MAX_VALUE)
                        .addComponent(panelEstadoGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        pestanas.addTab("Simulador", panelSimulador);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pestanas)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pestanas)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rbIOboundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbIOboundActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbIOboundActionPerformed

    private void btnVerGraficasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerGraficasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnVerGraficasActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Interfaz().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CPUestado;
    private javax.swing.JLabel CPUid1;
    private javax.swing.JLabel CPUmar;
    private javax.swing.JLabel CPUpc;
    private javax.swing.JLabel CPUproceso;
    private javax.swing.JButton aplicarPolitica;
    private javax.swing.JTextArea areaLog;
    private javax.swing.JProgressBar barraUsoCPU;
    private javax.swing.JButton btnCrearProceso;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPausar;
    private javax.swing.JButton btnReiniciar;
    private javax.swing.JButton btnVerGraficas;
    private javax.swing.JComboBox<String> comboPolitica;
    private javax.swing.ButtonGroup grupoTipoProceso;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelEstadoValor;
    private javax.swing.JLabel jLabelIDValor;
    private javax.swing.JLabel jLabelMARValor;
    private javax.swing.JLabel jLabelPCValor;
    private javax.swing.JLabel jLabelPoliticaActual;
    private javax.swing.JLabel jLabelProcesoValor;
    private javax.swing.JLabel jLabelRelojGlobal;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPanel panelCPU;
    private javax.swing.JPanel panelConfiguracion;
    private javax.swing.JPanel panelEstadisticas;
    private javax.swing.JPanel panelEstadoGlobal;
    private javax.swing.JPanel panelGraficos;
    private javax.swing.JPanel panelSimulador;
    private javax.swing.JTabbedPane pestanas;
    private javax.swing.JRadioButton rbCPUbound;
    private javax.swing.JRadioButton rbIObound;
    private javax.swing.JScrollPane scrollLog;
    private javax.swing.JSpinner spIOCadaN;
    private javax.swing.JSpinner spInstrucciones;
    private javax.swing.JTable tablaBloqueados;
    private javax.swing.JTable tablaBloqueadosSuspendidos;
    private javax.swing.JTable tablaListos;
    private javax.swing.JTable tablaListosSuspendidos;
    private javax.swing.JTable tablaTerminados;
    private javax.swing.JTextField txtNombreProc;
    // End of variables declaration//GEN-END:variables
}
