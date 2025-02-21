package com.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.JFreeChart;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private TextArea resultadoArea;

    public DatabaseConnection dbConnection;

    public GraficosController graficos;

    public MainController() {
        dbConnection = new DatabaseConnection();
        graficos = new GraficosController();
    }

    @FXML
    private VBox productosVBox;

    @FXML
    private VBox empleadosVBox;

    @FXML
    private VBox ventasVBox;

    @FXML
    private CheckBox Gproducto;

    @FXML
    private CheckBox Gempleado;

    @FXML
    public void sacarPDF(ActionEvent event) throws SQLException, DocumentException, FileNotFoundException {
        Document document = new Document();
        PdfWriter writer = null;

        try {
            // Crear archivo PDF y asociarlo al documento
            writer = PdfWriter.getInstance(document, new FileOutputStream("tabla.pdf"));
            document.open(); // Abrir documento para escribir

            if (Gproducto.isSelected()) {
                JFreeChart graficoProductos = graficos.crearGraficoVentasPorProducto();
                Image chartProductos = graficos.crearImagenDesdeGrafico(graficoProductos);
                System.out.println("Imagen generada con éxito. Tamaño: " +
                        chartProductos.getWidth() + "x" + chartProductos.getHeight());
                chartProductos.scaleToFit(500, 300);
                document.add(chartProductos);
            } else if (Gempleado.isSelected()) {
                JFreeChart graficoVentas = graficos.crearGraficoVentasPorEmpleado();
                Image chartEmpleados = graficos.crearImagenDesdeGrafico(graficoVentas);
                System.out.println("Imagen generada con éxito. Tamaño: " +
                        chartEmpleados.getWidth() + "x" + chartEmpleados.getHeight());
                chartEmpleados.scaleToFit(500, 300);
                document.add(chartEmpleados);
            }

            // Obtener las tablas generadas
            List<JTable> tablas = aplicarFiltros();
            if (tablas != null && !tablas.isEmpty()) {
                // Iterar sobre cada JTable en la lista
                for (JTable tabla : tablas) {
                    // Crear una PdfPTable con el número de columnas de la JTable
                    PdfPTable pdfTable = new PdfPTable(tabla.getColumnCount());

                    // Agregar los encabezados de columna
                    for (int i = 0; i < tabla.getColumnCount(); i++) {
                        pdfTable.addCell(tabla.getColumnName(i));
                    }

                    // Agregar las filas de datos
                    for (int i = 0; i < tabla.getRowCount(); i++) {
                        for (int j = 0; j < tabla.getColumnCount(); j++) {
                            pdfTable.addCell(tabla.getValueAt(i, j).toString());
                        }
                    }

                    // Agregar la tabla al documento
                    document.add(pdfTable);
                    // Añadir una nueva línea entre tablas para separarlas
                    document.add(new Paragraph("\n"));
                }
            } else {
                JOptionPane.showMessageDialog(null, "No hay datos para exportar.");
            }

        } catch (Exception e) {
            // Mostrar el mensaje de error
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage());
            e.printStackTrace(); // Imprimir el stack trace para depurar
        } finally {
            // Asegúrate de que el documento se cierra en caso de error o éxito
            if (document.isOpen()) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }

        // Mensaje al finalizar la operación con éxito
        JOptionPane.showMessageDialog(null, "PDF generado exitosamente.");
    }

    public void sacarExcel(ActionEvent event) {
        List<JTable> tablas = aplicarFiltros(); // Obtener las tablas generadas
        if (tablas != null && !tablas.isEmpty()) {
            try {
                // Crear un libro de trabajo Excel
                Workbook workbook = new XSSFWorkbook();

                // Iterar sobre cada JTable en la lista
                for (int i = 0; i < tablas.size(); i++) {
                    JTable tabla = tablas.get(i);

                    // Crear una hoja para cada JTable
                    Sheet sheet = workbook.createSheet("Tabla " + (i + 1));

                    // Crear una fila para los encabezados de columna
                    Row headerRow = sheet.createRow(0);
                    for (int j = 0; j < tabla.getColumnCount(); j++) {
                        // Crear la celda para el encabezado
                        Cell cell = headerRow.createCell(j);
                        cell.setCellValue(tabla.getColumnName(j));
                    }

                    // Llenar las filas con los datos de la tabla
                    for (int rowIndex = 0; rowIndex < tabla.getRowCount(); rowIndex++) {
                        Row row = sheet.createRow(rowIndex + 1); // Empezamos en la fila 1 porque la fila 0 es para los
                                                                 // encabezados
                        for (int colIndex = 0; colIndex < tabla.getColumnCount(); colIndex++) {
                            // Crear la celda para cada dato de la fila
                            Cell cell = row.createCell(colIndex);
                            // Establecer el valor de la celda
                            cell.setCellValue(tabla.getValueAt(rowIndex, colIndex).toString());
                        }
                    }
                }

                // Guardar el archivo Excel
                FileOutputStream fileOut = new FileOutputStream("tablas.xlsx");
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                JOptionPane.showMessageDialog(null, "Archivo Excel generado exitosamente.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al generar el archivo Excel: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "No hay datos para exportar.");
        }
    }

    public List<JTable> aplicarFiltros() {
        // Obtener los filtros seleccionados de cada VBox
        List<String> productosSeleccionados = getSelectedCheckBoxes(productosVBox);
        List<String> empleadosSeleccionados = getSelectedCheckBoxes(empleadosVBox);
        List<String> ventasSeleccionados = getSelectedCheckBoxes(ventasVBox);

        resultadoArea.setText("Filtros Productos: " + String.join(", ", productosSeleccionados));
        resultadoArea.setText("Filtros Empleados: " + String.join(", ", empleadosSeleccionados));
        resultadoArea.setText("Filtros Ventas: " + String.join(", ", ventasSeleccionados));

        String sql = "";
        List<JTable> tablasGeneradas = new ArrayList<>();

        if (!productosSeleccionados.isEmpty()) {
            sql = "SELECT " + String.join(", ", productosSeleccionados) + " FROM Productos";
            tablasGeneradas.add(mostrarDatos(sql));
        }
        if (!empleadosSeleccionados.isEmpty()) {
            sql = "SELECT " + String.join(", ", empleadosSeleccionados) + " FROM Empleados";
            tablasGeneradas.add(mostrarDatos(sql));
        }
        if (!ventasSeleccionados.isEmpty()) {
            sql = "SELECT " + String.join(", ", ventasSeleccionados) + " FROM Ventas";
            JTable tablaVentas = mostrarDatos(sql);
            tablasGeneradas.add(tablaVentas);
        }
        return tablasGeneradas;
    }

    // Método para obtener los checkboxes seleccionados dentro de un VBox
    private List<String> getSelectedCheckBoxes(VBox vbox) {
        List<String> selected = new ArrayList<>();

        for (Node node : vbox.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                selected.add(checkBox.getText());
            }
        }

        return selected;
    }

    public JTable mostrarDatos(String sql) {
        JTable table = null; // Inicializar la tabla
        try {
            // Ejecutar consulta
            ResultSet resultSet = dbConnection.executeQuery(sql);

            // Obtener metadatos de la consulta
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount(); // Número de columnas

            // Crear modelo de tabla
            DefaultTableModel model = new DefaultTableModel();

            // Agregar nombres de columnas al modelo
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Agregar filas de datos al modelo
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = resultSet.getObject(i);
                }
                model.addRow(rowData);
            }

            // Crear JTable con el modelo
            table = new JTable(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al ejecutar la consulta: " + e.getMessage());
        }

        return table;
    }

    @FXML
    private void marcarCheckBoxes(ActionEvent event) {
        // Obtenemos el botón que fue presionado
        Button boton = (Button) event.getSource();

        // Dependiendo del id del botón, marcamos los checkboxes correspondientes
        if ("botonProductos".equals(boton.getId())) {
            marcarCheckBoxesEnVBox(productosVBox); // Marcar checkboxes de productos
        } else if ("botonEmpleados".equals(boton.getId())) {
            marcarCheckBoxesEnVBox(empleadosVBox); // Marcar checkboxes de empleados
        } else if ("botonVentas".equals(boton.getId())) {
            marcarCheckBoxesEnVBox(ventasVBox);
        }
    }

    public void marcarCheckBoxesEnVBox(VBox vbox) {
        boolean todos = true;

        // Verificamos si todos los checkboxes están marcados
        for (var node : vbox.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (!checkBox.isSelected()) {
                    todos = false; // Si al menos uno no está marcado, cambiamos a false
                }
            }
        }

        // Si todos están marcados, desmarcamos todos; si no, marcamos todos
        for (var node : vbox.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (todos) {
                    // Si todos están marcados, desmarcamos
                    checkBox.setSelected(false);
                } else {
                    // Si al menos uno está desmarcado, marcamos todos
                    checkBox.setSelected(true);
                }
            }
        }
    }

}