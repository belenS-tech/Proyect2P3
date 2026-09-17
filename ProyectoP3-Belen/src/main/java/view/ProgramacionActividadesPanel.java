package view;


import model.FilaActividad;
import report.PdfReportService;
import report.ReportException;
import report.ReportHeader;
import report.ReportTable;
import repository.ReservaRepository;
import service.ProgramacionActividadesService;
import model.ResultadoProgramacionActividades;
import util.DateUtils;
import service.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProgramacionActividadesPanel extends JPanel {

    private static final DateTimeFormatter FORMATO_ENCABEZADO_DIA = DateTimeFormatter.ofPattern("EEE dd/MM");

    private final JSpinner fechaReferenciaSpinner;
    private final JButton btnCargar;
    private final JButton btnImprimir;
    private final JLabel lblSemana;
    private final JLabel lblMensaje;
    private final JTable tablaMatriz;
    private final DefaultTableModel modeloTabla;

    private ProgramacionActividadesService service;
    private final PdfReportService pdfReportService = new PdfReportService();

    private ResultadoProgramacionActividades ultimoResultado;
    private LocalDate ultimaFechaReferencia;

    public ProgramacionActividadesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Programación Semanal de Actividades");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.add(titulo, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fechaReferenciaSpinner = new JSpinner(new SpinnerDateModel());
        fechaReferenciaSpinner.setEditor(new JSpinner.DateEditor(fechaReferenciaSpinner, "dd/MM/yyyy"));
        btnCargar = new JButton("Cargar");
        btnImprimir = new JButton("Imprimir");
        btnImprimir.setEnabled(false);

        URL urlImprimir = getClass().getResource("/icons/imprimir.png");
        if (urlImprimir != null) {
            ImageIcon original = new ImageIcon(urlImprimir);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnImprimir.setIcon(new ImageIcon(img));
        }

        URL urlCargar = getClass().getResource("/icons/cargar.png");
        if (urlCargar != null) {
            ImageIcon original = new ImageIcon(urlCargar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnCargar.setIcon(new ImageIcon(img));
        }

        panelFiltros.add(new JLabel("Fecha de referencia:"));
        panelFiltros.add(fechaReferenciaSpinner);
        panelFiltros.add(btnCargar);
        panelFiltros.add(btnImprimir);

        lblSemana = new JLabel(" ");
        lblSemana.setFont(lblSemana.getFont().deriveFont(Font.BOLD));

        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        panelInfo.add(panelFiltros);
        panelInfo.add(lblSemana);
        panelSuperior.add(panelInfo, BorderLayout.CENTER);

        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(new Color(180, 0, 0));

        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Hora");
        tablaMatriz = new JTable(modeloTabla);
        tablaMatriz.setRowHeight(28);

        add(panelSuperior, BorderLayout.NORTH);
        add(lblMensaje, BorderLayout.SOUTH);
        add(new JScrollPane(tablaMatriz), BorderLayout.CENTER);

        btnCargar.addActionListener(e -> onCargar());
        btnImprimir.addActionListener(e -> onImprimir());
        Tema.aplicar(this);
    }

    public void configurarDependencias(ReservaRepository reservaRepo) {
        this.service = new ProgramacionActividadesService(reservaRepo);
    }

    private void onCargar() {
        lblMensaje.setText(" ");
        java.util.Date fechaUtil = (java.util.Date) fechaReferenciaSpinner.getValue();
        LocalDate fechaReferencia = fechaUtil.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        try {
            ResultadoProgramacionActividades resultado = service.construirMatrizSemana(fechaReferencia);
            this.ultimoResultado = resultado;
            this.ultimaFechaReferencia = fechaReferencia;

            lblSemana.setText(DateUtils.etiquetaSemana(fechaReferencia));
            construirTabla(resultado);
            btnImprimir.setEnabled(true);

        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    private void construirTabla(ResultadoProgramacionActividades resultado) {
        modeloTabla.setColumnCount(0);
        modeloTabla.addColumn("Hora");
        for (LocalDate dia : resultado.getDias()) {
            modeloTabla.addColumn(dia.format(FORMATO_ENCABEZADO_DIA));
        }

        modeloTabla.setRowCount(0);
        for (FilaActividad fila : resultado.getFilas()) {
            List<Object> valores = new ArrayList<>();
            valores.add(fila.getHoraTexto());
            for (LocalDate dia : resultado.getDias()) {
                valores.add(fila.getValor(dia));
            }
            modeloTabla.addRow(valores.toArray());
        }
    }

    private void onImprimir() {
        if (ultimoResultado == null) {
            lblMensaje.setText("Primero debe cargar una semana con datos.");
            return;
        }

        try {
            List<String> encabezados = new ArrayList<>();
            encabezados.add("Hora");
            for (LocalDate dia : ultimoResultado.getDias()) {
                encabezados.add(dia.format(FORMATO_ENCABEZADO_DIA));
            }

            ReportTable tabla = new ReportTable(encabezados);
            for (FilaActividad fila : ultimoResultado.getFilas()) {
                List<String> valores = new ArrayList<>();
                valores.add(fila.getHoraTexto());
                for (LocalDate dia : ultimoResultado.getDias()) {
                    valores.add(fila.getValor(dia));
                }
                tabla.agregarFila(valores);
            }

            String filtros = DateUtils.etiquetaSemana(ultimaFechaReferencia);
            ReportHeader header = new ReportHeader(
                    "Programación Semanal de Actividades",
                    SessionManager.getInstancia().getUsuarioActual().getId(),
                    filtros
            );

            String nombreSugerido = "actividades_" + ultimaFechaReferencia + ".pdf";
            String ruta = elegirRutaGuardado(nombreSugerido);
            if (ruta == null) {
                return;
            }

            pdfReportService.generarReporteMatriz(ruta, header, tabla);
            lblMensaje.setForeground(new Color(0, 122, 47));
            lblMensaje.setText("Reporte generado en: " + ruta);

        } catch (ReportException e) {
            lblMensaje.setForeground(new Color(180, 0, 0));
            lblMensaje.setText("Error al generar el PDF: " + e.getMessage());
        }
    }
    /** Abre un diálogo "Guardar como" y devuelve la ruta elegida, o null si el usuario cancela. */
    private String elegirRutaGuardado(String nombreSugerido) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte PDF");
        selector.setSelectedFile(new java.io.File(nombreSugerido));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));

        int resultado = selector.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        java.io.File archivo = selector.getSelectedFile();
        String ruta = archivo.getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".pdf")) {
            ruta += ".pdf";
        }
        return ruta;
    }
}