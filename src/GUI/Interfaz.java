/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import Componentes.CPU;

import Estructuras.Cola;
import Componentes.GestorSistema;
import Componentes.Kernel;
import Componentes.MemoriaPrincipal;
import Componentes.PCB;
import Componentes.Planificador;
import Componentes.PerfilSimulacion;
import Componentes.Estado;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;

/**
 *
 * @author luismarianolovera
 */
public class Interfaz extends javax.swing.JFrame {
    
    private static final Logger logger = Logger.getLogger(Interfaz.class.getName());
    private GestorSistema gestor;
    private Kernel kernel;
    private Timer refrescadorUI; // Timer Swing que refresca la GUI cada cierto intervalo
    private Cola<PCB> bufferPreKernel = new Cola<PCB>();
    private boolean kernelIniciado = false;
    
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


        areaLog.setText("");
        rbCPUbound.setSelected(true);
        
        actualizarVisibilidadCamposIO();

        initSimulador();
        hookListeners();
        iniciarRefrescadorUI();
           
    }
    
    /**
     * Crea el GestorSistema con parámetros por defecto,
     * inicia el Kernel y la CPU, y guarda la referencia al kernel.
     */
    private void initSimulador() {
        PerfilSimulacion perfil = new PerfilSimulacion("./config.json");
        perfil.cargarDesdeJSON();

        gestor = new GestorSistema(
            perfil.getDuracionCicloMS(),
            perfil.getDuracionBloqueoIO(),
            perfil.getQuantumRR(),
            perfil.getPoliticaInicial(),
            perfil.getRafagaCPUDefault(),
            perfil.getIoCadaNDefault(),
            perfil.getPrioridadBase()
        );
        
        kernel = gestor.construirKernel();

        bufferPreKernel.vaciar();
        Cola<PCB> originales = perfil.getProcesosIniciales();
        int n = originales.verTamano();
        for (int i = 0; i < n; i++) {
            PCB p = originales.getAt(i);
            bufferPreKernel.encolar(p);
        }

        kernelIniciado=false;
        
        jLabelDuracionActual.setText(perfil.getDuracionCicloMS() + " ms");
        spDuracionCiclo.setValue(perfil.getDuracionCicloMS());
         
    }
    
    /**
     * Crea las acciones de los botones y combobox.
     */
    private void hookListeners() {

        btnIniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                kernel.iniciarKernel();

                if (!kernelIniciado) {
                    kernelIniciado = true;

                    while (!bufferPreKernel.esVacia()) {
                        PCB p = bufferPreKernel.desencolar();
                        kernel.admitirProceso(p);
                    }

                    JOptionPane.showMessageDialog(
                        Interfaz.this,
                        "Simulación iniciada.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        Interfaz.this,
                        "Simulación reanudada.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }

                // aseguramos refresco visual encendido
                if (refrescadorUI != null && !refrescadorUI.isRunning()) {
                    refrescadorUI.start();
                }
            }
        });
        
        btnPausar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (kernel != null) {
                kernel.pausarKernel();
            }
                
            if (refrescadorUI != null && refrescadorUI.isRunning()) {
                refrescadorUI.stop();
            }

            JOptionPane.showMessageDialog(
                Interfaz.this,
                "Simulación pausada.",
                "Pausa",
                JOptionPane.INFORMATION_MESSAGE
            );
         }
    });

        // Botón "Aplicar política"
        aplicarPolitica.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Planificador.PoliticaPlanificacion nueva = leerPoliticaDesdeCombo();
                kernel.cambiarPolitica(nueva);
            }
        });
        
        aplicarTiempo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            int nuevaDuracion = (Integer) spDuracionCiclo.getValue();

       
            if (kernel != null) {
                kernel.setDuracionCicloMS(nuevaDuracion);
                jLabelDuracionActual.setText(nuevaDuracion + " ms");
            }
        }   
    });

        // Botón "Crear proceso"
        btnCrearProceso.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

       
            String nombre = txtNombreProc.getText().trim();
            if (nombre.isEmpty()) {JOptionPane.showMessageDialog(Interfaz.this,"Ingrese un nombre para el proceso.","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            int instruccionesTotales = (Integer) spInstrucciones.getValue();
            boolean esCPUbound = rbCPUbound.isSelected();
            
            //Adaptacion, seleccion Tipo 
            int ioCadaN   = esCPUbound ? 0 : (Integer) spIOCadaN.getValue();
            int duracionES= esCPUbound ? 0 : (Integer) spDuracionIO.getValue();

            int pid = gestor.generarPID();

            // Modalidades
            
            // Ve si el kernel ya está corriendo
            boolean kernelActivo = kernelIniciado;

            // tiempoLlegada:si ya corre el kernel usamos kernel.getReloj(). si no corre todavía, le ponemos 0
            int llegada = kernelActivo ? kernel.getReloj() : 0;

            PCB pcbNuevo = new PCB(
                pid,
                nombre,
                instruccionesTotales,
                gestor.getPrioridadBase(),
                llegada,      // tiempoLlegada
                ioCadaN,
                duracionES
            );
            
            pcbNuevo.setEstado(Estado.NUEVO);

            if (kernelActivo) {
                kernel.admitirProceso(pcbNuevo);
            } else {
                bufferPreKernel.encolar(pcbNuevo);
            }

            // Limpieza de interfaz
            txtNombreProc.setText("");
            spInstrucciones.setValue(1);
            spDuracionIO.setValue(1);
            spIOCadaN.setValue(1);
            rbCPUbound.setSelected(true);
            actualizarVisibilidadCamposIO();

            JOptionPane.showMessageDialog(Interfaz.this,"Proceso preparado.","OK",JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        rbCPUbound.addActionListener(new ActionListener() {
        @Override
        
        public void actionPerformed(ActionEvent e) {
            actualizarVisibilidadCamposIO();
            }
        });

        rbIObound.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            actualizarVisibilidadCamposIO();
            }
        });
        
        
    }

    /**
     * Traduce lo que hay seleccionado en comboPolitica al enum del Planificador.
     */
    private Planificador.PoliticaPlanificacion leerPoliticaDesdeCombo() {
        String sel = (String) comboPolitica.getSelectedItem();
        if (sel == null) {
            return Planificador.PoliticaPlanificacion.FCFS;
        }
        switch (sel) {
            case "FCFS":
                return Planificador.PoliticaPlanificacion.FCFS;
            case "Round Robin":
                return Planificador.PoliticaPlanificacion.RR;
            case "SPN":
                return Planificador.PoliticaPlanificacion.SPN;
            case "SRT":
                return Planificador.PoliticaPlanificacion.SRT;
            case "HRRN":
                return Planificador.PoliticaPlanificacion.HRRN;
            case "Feedback":
                return Planificador.PoliticaPlanificacion.FEEDBACK;
            default:
                return Planificador.PoliticaPlanificacion.FCFS;
        }
    }
    
    /**
     * Arranca un Timer Swing que refresca la pantalla cada ~200ms.
     * (Esto es la "foto del estado actual" del SO en ese tick).
     */
    private void iniciarRefrescadorUI() {
        refrescadorUI = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarPantalla();
            }
        });
        refrescadorUI.start();
    }

    /**
     * Refresca TODO en la pantalla con el estado ACTUAL del Kernel.
     * - labels de estado global
     * - info de la CPU
     * - tablas de colas
     * - log de eventos
     */
    private void refrescarPantalla() {
        if (kernel == null) return;

        // 1. Estado global
        actualizarEstadoGlobal(String.valueOf(kernel.getReloj()), kernel.getPlanificador().nombre());

        // 2. CPU actual
        CPU cpu = kernel.getCPU();
        PCB p = cpu.getProcesoActual();
        String modoCPUTexto = (p != null) ? "Usuario" : "SO";

        actualizarCPU(
            (p != null ? p.getNombre() : null),
            (p != null ? String.valueOf(p.getId()) : null),
            (p != null ? String.valueOf(p.getPc()) : null),
            (p != null ? String.valueOf(p.getMAR()) : null),
            (p != null ? p.getEstado().toString() : "IDLE"),
            modoCPUTexto,
            (p != null ? 100 : 0)
        );

        // 3. Tablas de colas
        MemoriaPrincipal mem = kernel.getMemoria();
        Planificador plan = kernel.getPlanificador();

        Object[][] tablaListosData;
        if (plan.esFeedback()) {
            tablaListosData = plan.obtenerTablaListosFeedback();
        } else {
            tablaListosData = mem.obtenerTablaListos();
        }

        refrescarTabla(tablaListos, tablaListosData);
        refrescarTabla(tablaBloqueados, mem.obtenerTablaBloqueados());
        refrescarTabla(tablaListosSuspendidos, mem.obtenerTablaListosSuspendidos());
        refrescarTabla(tablaBloqueadosSuspendidos, mem.obtenerTablaBloqueadosSuspendidos());
        refrescarTabla(tablaTerminados, mem.obtenerTablaTerminados());
        
        while (!kernel.getLogEventos().esVacia()) {
            String linea = kernel.getLogEventos().desencolar();
            logEvento(linea);
        }
    }
        
    public void actualizarCPU(
        String nombreProceso,
        String pid,
        String pc,
        String mar,
        String estado,
        String modoCPU,
        int usoCPU
            
    ) {
        jLabelProcesoValor.setText(nombreProceso != null ? nombreProceso : "-");
        jLabelIDValor.setText(pid != null ? pid : "-");
        jLabelPCValor.setText(pc != null ? pc : "-"); // si renombras este a jLabelPCValor, actualiza aquí
        jLabelMARValor.setText(mar != null ? mar : "-");
        jLabelEstadoValor.setText(estado != null ? estado : "-");
        jLabelModoCPUValor.setText(modoCPU != null ? modoCPU : "-");

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
    
    private void actualizarVisibilidadCamposIO() {
        boolean io = rbIObound.isSelected();

        // Campos relacionados con I/O:
        jLabel22.setEnabled(io);         // "I/O cada N ciclos:"
        spIOCadaN.setEnabled(io);

        jLabel23.setEnabled(io); // "Duración E/S (ciclos):"
        spDuracionIO.setEnabled(io);
    }
    
    private void guardarPerfilActualEnJSON() {
        PerfilSimulacion perfil = new PerfilSimulacion("./config.json");

        perfil.setDuracionCicloMS(gestor.getDuracionCicloMS());
        perfil.setDuracionBloqueoIO(gestor.getDuracionBloqueoIO());
        perfil.setQuantumRR(gestor.getQuantumRR());
        perfil.setPoliticaInicial(gestor.getPoliticaInicial());
        perfil.setRafagaCPUDefault(gestor.getRafagaCPUDefault());
        perfil.setIoCadaNDefault(gestor.getIoCadaNDefault());
        perfil.setPrioridadBase(gestor.getPrioridadBase());

        Cola<PCB> originales = gestor.getProcesosCargados();

        Cola<PCB> copia = new Cola<PCB>();
        int n = originales.verTamano();
        int i = 0;
        while (i < n) {
            PCB p = originales.getAt(i);
         copia.encolar(p);
            i = i + 1;
        }

        perfil.setProcesosIniciales(copia);

        // 4. guardar
        boolean ok = perfil.guardarEnJSON();

        // 5. feedback visual
        if (ok) {
            javax.swing.JOptionPane.showMessageDialog(this,"Perfil guardado en config.json","OK",javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
        javax.swing.JOptionPane.showMessageDialog(this,"Error al guardar el perfil","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
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
        jLabelModoCPUTitulo = new javax.swing.JLabel();
        jLabelModoCPUValor = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        btnIniciar = new javax.swing.JButton();
        btnPausar = new javax.swing.JButton();
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
        jLabelRelojGlobal = new javax.swing.JLabel();
        jLabelPoliticaActual = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabelDuracionActual = new javax.swing.JLabel();
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
        btnCrearProceso = new javax.swing.JButton();
        spIOCadaN = new javax.swing.JSpinner();
        spDuracionIO = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        panelEstadoGlobal1 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        comboPolitica = new javax.swing.JComboBox<>();
        aplicarPolitica = new javax.swing.JButton();
        jLabel25 = new javax.swing.JLabel();
        spDuracionCiclo = new javax.swing.JSpinner();
        aplicarTiempo = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btnGuardarPerfil1 = new javax.swing.JButton();
        panelGraficos = new javax.swing.JPanel();
        btnGrafTurnaround = new javax.swing.JButton();
        btnGrafUsoCPU = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        btnGrafUsoCPU1 = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelCPU.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel1.setText("CPU actual");

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel2.setText("Proceso:");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel3.setText("ID: ");

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel4.setText("PC:");

        jLabel5.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel5.setText("MAR:");

        jLabel6.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel6.setText("Estado:");

        jLabelProcesoValor.setText("-");

        jLabelIDValor.setText("-");

        jLabelPCValor.setText("-");

        jLabelEstadoValor.setText("-");

        jLabelMARValor.setText("-");

        barraUsoCPU.setStringPainted(true);

        jLabelModoCPUTitulo.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabelModoCPUTitulo.setText("Modo CPU:");

        jLabelModoCPUValor.setText("-");

        javax.swing.GroupLayout panelCPULayout = new javax.swing.GroupLayout(panelCPU);
        panelCPU.setLayout(panelCPULayout);
        panelCPULayout.setHorizontalGroup(
            panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCPULayout.createSequentialGroup()
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCPULayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelCPULayout.createSequentialGroup()
                                .addComponent(jLabelModoCPUTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelModoCPUValor, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(barraUsoCPU, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelCPULayout.createSequentialGroup()
                                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabelProcesoValor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(22, 22, 22)
                        .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CPUproceso, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CPUid1)
                            .addComponent(CPUpc)
                            .addComponent(CPUmar)
                            .addComponent(CPUestado)))
                    .addGroup(panelCPULayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jLabel1)))
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
                .addGap(18, 18, 18)
                .addGroup(panelCPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelModoCPUTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelModoCPUValor))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel7.setText("Controles");

        btnIniciar.setText("Iniciar");

        btnPausar.setText("Pausar");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(110, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addGap(148, 148, 148))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPausar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnIniciar)
                    .addComponent(btnPausar))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jLabel8.setFont(new java.awt.Font("Helvetica Neue", 3, 18)); // NOI18N
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

        jLabel9.setFont(new java.awt.Font("Helvetica Neue", 3, 18)); // NOI18N
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

        jLabel10.setFont(new java.awt.Font("Helvetica Neue", 3, 18)); // NOI18N
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

        jLabel11.setFont(new java.awt.Font("Helvetica Neue", 3, 18)); // NOI18N
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

        jLabel12.setFont(new java.awt.Font("Helvetica Neue", 3, 18)); // NOI18N
        jLabel12.setText("Cola Terminados");

        panelEstadoGlobal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel13.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel13.setText("Estado global");

        jLabel14.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel14.setText("Ciclo de reloj:");

        jLabel15.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel15.setText("Política actual:");

        jLabelRelojGlobal.setText("-");

        jLabelPoliticaActual.setText("-");

        jLabel26.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel26.setText("Duración ciclo actual:");

        jLabelDuracionActual.setText("-");

        javax.swing.GroupLayout panelEstadoGlobalLayout = new javax.swing.GroupLayout(panelEstadoGlobal);
        panelEstadoGlobal.setLayout(panelEstadoGlobalLayout);
        panelEstadoGlobalLayout.setHorizontalGroup(
            panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel14)
                            .addComponent(jLabel26))
                        .addGap(60, 60, 60)
                        .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelRelojGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelPoliticaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelDuracionActual, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(jLabel13)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEstadoGlobalLayout.setVerticalGroup(
            panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoGlobalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabelRelojGlobal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabelPoliticaActual))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEstadoGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(jLabelDuracionActual))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel17.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
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
                .addGap(15, 15, 15)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel17)
                .addGap(40, 40, 40))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel17)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel18.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel18.setText("Crear proceso");

        jLabel19.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel19.setText("Tipo:");

        jLabel20.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel20.setText("Nombre:");

        spInstrucciones.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));

        jLabel21.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
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

        jLabel22.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel22.setText("I/O cada N ciclos:");

        btnCrearProceso.setText("Crear proceso");

        spIOCadaN.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));

        spDuracionIO.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));

        jLabel23.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel23.setText("Duración de E/S (ciclos):");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCrearProceso, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(97, 97, 97))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(rbCPUbound)
                                .addGap(18, 18, 18)
                                .addComponent(rbIObound))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(spIOCadaN)
                                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                                .addGap(34, 34, 34)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(spDuracionIO, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(16, 16, 16))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNombreProc, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(spInstrucciones, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(105, 105, 105)
                .addComponent(jLabel18)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombreProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spInstrucciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCPUbound)
                    .addComponent(rbIObound))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jLabel22))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(spIOCadaN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(btnCrearProceso)
                        .addGap(14, 14, 14))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(spDuracionIO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        panelEstadoGlobal1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel24.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel24.setText("Modificaciones en tiempo real");

        jLabel16.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel16.setText("Cambiar política de planificación:");

        comboPolitica.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "SPN", "SRT", "HRRN", "Feedback" }));

        aplicarPolitica.setText("Aplicar política");

        jLabel25.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel25.setText("Cambiar duración de un ciclo en ejecución (en ms):");

        spDuracionCiclo.setModel(new javax.swing.SpinnerNumberModel(200, 1, 5000, 50));

        aplicarTiempo.setText("Aplicar cambio");

        javax.swing.GroupLayout panelEstadoGlobal1Layout = new javax.swing.GroupLayout(panelEstadoGlobal1);
        panelEstadoGlobal1.setLayout(panelEstadoGlobal1Layout);
        panelEstadoGlobal1Layout.setHorizontalGroup(
            panelEstadoGlobal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoGlobal1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEstadoGlobal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addGroup(panelEstadoGlobal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelEstadoGlobal1Layout.createSequentialGroup()
                            .addComponent(comboPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(aplicarPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelEstadoGlobal1Layout.createSequentialGroup()
                            .addComponent(spDuracionCiclo, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(aplicarTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.LEADING)))
                .addContainerGap(18, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEstadoGlobal1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel24)
                .addGap(40, 40, 40))
        );
        panelEstadoGlobal1Layout.setVerticalGroup(
            panelEstadoGlobal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoGlobal1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelEstadoGlobal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aplicarPolitica))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEstadoGlobal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spDuracionCiclo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aplicarTiempo))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnGuardarPerfil1.setText("Guardar perfil a JSON");
        btnGuardarPerfil1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarPerfil1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGuardarPerfil1)
                .addGap(26, 26, 26))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(btnGuardarPerfil1)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelSimuladorLayout = new javax.swing.GroupLayout(panelSimulador);
        panelSimulador.setLayout(panelSimuladorLayout);
        panelSimuladorLayout.setHorizontalGroup(
            panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSimuladorLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelEstadoGlobal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelEstadoGlobal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48)
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSimuladorLayout.createSequentialGroup()
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane6)
                                .addComponent(jScrollPane5)
                                .addComponent(jLabel10)
                                .addComponent(jLabel9)
                                .addComponent(jLabel11)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(scrollLog, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 32, Short.MAX_VALUE)
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(panelCPU, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(57, 57, 57))
                    .addGroup(panelSimuladorLayout.createSequentialGroup()
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel12))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        panelSimuladorLayout.setVerticalGroup(
            panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSimuladorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelSimuladorLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelCPU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelSimuladorLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollLog, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelSimuladorLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelSimuladorLayout.createSequentialGroup()
                        .addGroup(panelSimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelSimuladorLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(panelEstadoGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelEstadoGlobal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 46, Short.MAX_VALUE))
        );

        pestanas.addTab("Simulador", panelSimulador);

        btnGrafTurnaround.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnGrafTurnaround.setText("Ver gráfica de procesos completados por unidad de tiempo");
        btnGrafTurnaround.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrafTurnaroundActionPerformed(evt);
            }
        });

        btnGrafUsoCPU.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnGrafUsoCPU.setText("Ver gráfica de uso del CPU");
        btnGrafUsoCPU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrafUsoCPUActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel27.setText("Gráficas");

        btnGrafUsoCPU1.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnGrafUsoCPU1.setText("Ver datos");
        btnGrafUsoCPU1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrafUsoCPU1ActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel28.setText("Métricas");

        javax.swing.GroupLayout panelGraficosLayout = new javax.swing.GroupLayout(panelGraficos);
        panelGraficos.setLayout(panelGraficosLayout);
        panelGraficosLayout.setHorizontalGroup(
            panelGraficosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGraficosLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(panelGraficosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGrafUsoCPU1, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addGroup(panelGraficosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel27)
                        .addComponent(btnGrafTurnaround, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnGrafUsoCPU, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(845, Short.MAX_VALUE))
        );
        panelGraficosLayout.setVerticalGroup(
            panelGraficosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGraficosLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel27)
                .addGap(32, 32, 32)
                .addComponent(btnGrafTurnaround, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnGrafUsoCPU, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jLabel28)
                .addGap(18, 18, 18)
                .addComponent(btnGrafUsoCPU1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(425, Short.MAX_VALUE))
        );

        pestanas.addTab("Gráficos y Métricas", panelGraficos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(pestanas, javax.swing.GroupLayout.PREFERRED_SIZE, 1302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pestanas)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGrafTurnaroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrafTurnaroundActionPerformed
        if (kernel == null) {
            System.out.println("No hay kernel corriendo");
        return;
        }

        Cola<PCB> colaTerminados = kernel.getMemoria().getTerminadosCola();

        Graficos.GraficarSimulacion.graficaTurnaround(colaTerminados);
    }//GEN-LAST:event_btnGrafTurnaroundActionPerformed

    private void btnGrafUsoCPUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrafUsoCPUActionPerformed
        if (kernel == null) {
            System.out.println("No hay kernel corriendo");
        return;
        }
        Graficos.GraficarSimulacion.graficaUsoCPU(kernel.getHistTiempo(),kernel.getHistUsoCPU(),kernel.getHistCount());
    }//GEN-LAST:event_btnGrafUsoCPUActionPerformed

    private void rbIOboundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbIOboundActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbIOboundActionPerformed

    private void btnGuardarPerfil1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarPerfil1ActionPerformed
        // TODO add your handling code here:
         guardarPerfilActualEnJSON();
    }//GEN-LAST:event_btnGuardarPerfil1ActionPerformed

    private void btnGrafUsoCPU1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrafUsoCPU1ActionPerformed
        if (kernel == null) {
            JOptionPane.showMessageDialog(
            this,
            "El kernel todavía no está inicializado.",
            "Estadísticas",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String resumen = kernel.getResumenEstadisticas();

    JOptionPane.showMessageDialog(
        this,
        resumen,
        "Estadísticas actuales",
        JOptionPane.INFORMATION_MESSAGE
    ); 
    }//GEN-LAST:event_btnGrafUsoCPU1ActionPerformed

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
    private javax.swing.JButton aplicarTiempo;
    private javax.swing.JTextArea areaLog;
    private javax.swing.JProgressBar barraUsoCPU;
    private javax.swing.JButton btnCrearProceso;
    private javax.swing.JButton btnGrafTurnaround;
    private javax.swing.JButton btnGrafUsoCPU;
    private javax.swing.JButton btnGrafUsoCPU1;
    private javax.swing.JButton btnGuardarPerfil1;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPausar;
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
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDuracionActual;
    private javax.swing.JLabel jLabelEstadoValor;
    private javax.swing.JLabel jLabelIDValor;
    private javax.swing.JLabel jLabelMARValor;
    private javax.swing.JLabel jLabelModoCPUTitulo;
    private javax.swing.JLabel jLabelModoCPUValor;
    private javax.swing.JLabel jLabelPCValor;
    private javax.swing.JLabel jLabelPoliticaActual;
    private javax.swing.JLabel jLabelProcesoValor;
    private javax.swing.JLabel jLabelRelojGlobal;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPanel panelCPU;
    private javax.swing.JPanel panelEstadoGlobal;
    private javax.swing.JPanel panelEstadoGlobal1;
    private javax.swing.JPanel panelGraficos;
    private javax.swing.JPanel panelSimulador;
    private javax.swing.JTabbedPane pestanas;
    private javax.swing.JRadioButton rbCPUbound;
    private javax.swing.JRadioButton rbIObound;
    private javax.swing.JScrollPane scrollLog;
    private javax.swing.JSpinner spDuracionCiclo;
    private javax.swing.JSpinner spDuracionIO;
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
