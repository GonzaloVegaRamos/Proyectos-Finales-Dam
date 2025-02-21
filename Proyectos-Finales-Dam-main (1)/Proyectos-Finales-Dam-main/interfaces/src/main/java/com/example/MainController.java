package com.example;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.JFreeChart;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class MainController {

    public DatabaseConnection dbConnection;

    public GraficosController graficos;

    public MainController() {
        dbConnection = new DatabaseConnection();
        graficos = new GraficosController();

    }

    @FXML
    public CheckBox Gproducto;

    @FXML
    public CheckBox Gempleado;

    @FXML
    public VBox productosVBox;

    @FXML
    public VBox empleadosVBox;

    @FXML
    public VBox ventasVBox;

    @FXML
    public void sacarPDF(ActionEvent event) throws SQLException, DocumentException, FileNotFoundException {
        Document document = new Document();
        PdfWriter writer = null;

        try {
            // Crear archivo PDF y asociarlo al documento
            writer = PdfWriter.getInstance(document, new FileOutputStream("tabla.pdf"));
            document.open(); // Abrir documento para escribir

            // Título del documento
            Paragraph tituloReporte = new Paragraph("Reporte del Mes",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            tituloReporte.setAlignment(Element.ALIGN_CENTER);
            document.add(tituloReporte);

            // Añadir una línea separadora después del título
            document.add(new Chunk("\n"));
            document.add(new LineSeparator());
            document.add(new Chunk("\n"));

            // Gráficas
            if (Gproducto.isSelected()) {

                JFreeChart graficoProductos = graficos.crearGraficoVentasPorProducto();
                Image chartProductos = graficos.crearImagenDesdeGrafico(graficoProductos);
                System.out.println("Imagen generada con éxito. Tamaño: " +
                        chartProductos.getWidth() + "x" + chartProductos.getHeight());
                chartProductos.scaleToFit(500, 300);
                document.add(chartProductos);
                document.add(new Chunk("\n"));
            }

            if (Gempleado.isSelected()) {

                JFreeChart graficoVentas = graficos.crearGraficoVentasPorEmpleado();
                Image chartEmpleados = graficos.crearImagenDesdeGrafico(graficoVentas);
                System.out.println("Imagen generada con éxito. Tamaño: " +
                        chartEmpleados.getWidth() + "x" + chartEmpleados.getHeight());
                chartEmpleados.scaleToFit(500, 300);
                document.add(chartEmpleados);
                document.add(new Chunk("\n"));
            }

            // Obtener las tablas generadas
            List<TablaConEtiqueta> tablas = aplicarFiltros();
            if (tablas != null && !tablas.isEmpty()) {
                // Iterar sobre cada JTable en la lista
                for (TablaConEtiqueta tabla : tablas) {

                    if (tabla.etiqueta == "productos") {
                        Paragraph tituloTabla = new Paragraph("Datos de la Tabla PRODUCTOS",
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                        tituloTabla.setAlignment(Element.ALIGN_LEFT);
                        document.add(tituloTabla);
                        document.add(new Chunk("\n"));
                    }

                    if (tabla.etiqueta == "empleados") {
                        Paragraph tituloTabla = new Paragraph("Datos de la Tabla EMPLEADOS",
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                        tituloTabla.setAlignment(Element.ALIGN_LEFT);
                        document.add(tituloTabla);
                        document.add(new Chunk("\n"));
                    }

                    if (tabla.etiqueta == "ventas") {
                        Paragraph tituloTabla = new Paragraph("Datos de la Tabla VENTAS",
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                        tituloTabla.setAlignment(Element.ALIGN_LEFT);
                        document.add(tituloTabla);
                        document.add(new Chunk("\n"));
                    }

                    // Crear una PdfPTable con el número de columnas de la JTable
                    PdfPTable pdfTable = new PdfPTable(tabla.tabla.getColumnCount());
                    pdfTable.setWidthPercentage(100); // Ocupa todo el ancho de la página

                    // Agregar los encabezados de columna con estilo
                    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                    for (int i = 0; i < tabla.tabla.getColumnCount(); i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(tabla.tabla.getColumnName(i), headerFont));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY); // Fondo gris para los encabezados
                        pdfTable.addCell(cell);
                    }

                    // Agregar las filas de datos
                    for (int i = 0; i < tabla.tabla.getRowCount(); i++) {
                        for (int j = 0; j < tabla.tabla.getColumnCount(); j++) {
                            PdfPCell cell = new PdfPCell(new Phrase(tabla.tabla.getValueAt(i, j).toString()));
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            pdfTable.addCell(cell);
                        }
                    }

                    // Agregar la tabla al documento
                    document.add(pdfTable);

                    // Añadir una línea separadora después de cada tabla
                    document.add(new Chunk("\n"));
                    document.add(new LineSeparator());
                    document.add(new Chunk("\n"));
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

    public void sacarExcel(ActionEvent event) throws SQLException, IOException {
        // Crear un libro de trabajo (workbook)
        Workbook workbook = new XSSFWorkbook();

        // Crear un contenedor de gráficos en la hoja
        XSSFDrawing drawing;

        // Obtener las tablas generadas
        List<TablaConEtiqueta> tablas = aplicarFiltros();
        if (tablas != null && !tablas.isEmpty()) {
            int sheetIndex = 0; // Para llevar el control de las hojas creadas

            // Iterar sobre cada tabla en la lista
            for (TablaConEtiqueta tabla : tablas) {
                // Crear una nueva hoja para cada tabla
                XSSFSheet sheet = (XSSFSheet) workbook.createSheet(tabla.getEtiqueta());

                // Título de la hoja (Tabla: Productos, Empleados, Ventas, etc.)
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue("Informe de " + tabla.getEtiqueta()); // Título de la tabla

                // Agregar gráfico si es necesario
                drawing = sheet.createDrawingPatriarch();
                if ("Productos".equals(tabla.getEtiqueta()) && Gproducto.isSelected()) {
                    JFreeChart graficoProductos = graficos.crearGraficoVentasPorProducto();
                    com.itextpdf.text.Image chartProductos = graficos.crearImagenDesdeGrafico(graficoProductos);
                    BufferedImage bufferedImage = convertirABufferedImage(chartProductos);
                    byte[] imageBytes = imageToByteArray(bufferedImage);
                    int pictureIdx = ((XSSFWorkbook) workbook).addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
                    ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 1, 5, 6);
                    XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
                    picture.resize(1.0);
                } else if ("Empleados".equals(tabla.getEtiqueta()) && Gempleado.isSelected()) {
                    JFreeChart graficoVentas = graficos.crearGraficoVentasPorEmpleado();
                    com.itextpdf.text.Image chartEmpleados = graficos.crearImagenDesdeGrafico(graficoVentas);
                    BufferedImage bufferedImage = convertirABufferedImage(chartEmpleados);
                    byte[] imageBytes = imageToByteArray(bufferedImage);
                    int pictureIdx = ((XSSFWorkbook) workbook).addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
                    ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 1, 5, 6);
                    XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
                    picture.resize(1.0);
                }

                // Comenzamos a agregar la tabla a la hoja
                int rowIndex = 2; // Fila donde empieza la tabla (después del título)

                // Agregar los encabezados de columna
                // Agregar los encabezados de columna (todos en una sola fila)
                Row headerRow = sheet.createRow(rowIndex++);
                for (int i = 0; i < tabla.getTabla().getColumnCount(); i++) {
                    Cell headerCell = headerRow.createCell(i);
                    headerCell.setCellValue(tabla.getTabla().getColumnName(i));
                }

                // Agregar las filas de datos
                for (int i = 0; i < tabla.getTabla().getRowCount(); i++) {
                    row = sheet.createRow(rowIndex++);
                    for (int j = 0; j < tabla.getTabla().getColumnCount(); j++) {
                        cell = row.createCell(j);
                        // Manejar el tipo de dato de cada celda
                        Object value = tabla.getTabla().getValueAt(i, j);
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Date) {
                            cell.setCellValue((Date) value);
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                // Añadir un salto de línea entre tablas (agregar una fila vacía) si es
                // necesario
                sheet.createRow(rowIndex++);
            }
        } else {
            JOptionPane.showMessageDialog(null, "No hay datos para exportar.");
        }

        // Guardar el archivo Excel
        try (FileOutputStream fileOut = new FileOutputStream("informe.xlsx")) {
            workbook.write(fileOut);
        }

        // Cerrar el workbook
        workbook.close();

        // Mensaje al finalizar la operación con éxito
        JOptionPane.showMessageDialog(null, "Excel generado exitosamente.");
    }

    // Método para convertir la imagen de iText a BufferedImage
    private BufferedImage convertirABufferedImage(com.itextpdf.text.Image iTextImage) {
        try {
            // Convertir la imagen iText en bytes
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iTextImage.getOriginalData());

            // Leer los bytes de la imagen y convertirlos en BufferedImage
            return ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para convertir BufferedImage a array de bytes
    private byte[] imageToByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
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

    @FXML
    private TextArea resultadoArea;

    public List<TablaConEtiqueta> aplicarFiltros() {
        // Obtener los filtros seleccionados de cada VBox
        List<String> productosSeleccionados = getSelectedCheckBoxes(productosVBox);
        List<String> empleadosSeleccionados = getSelectedCheckBoxes(empleadosVBox);
        List<String> ventasSeleccionados = getSelectedCheckBoxes(ventasVBox);

        resultadoArea.setText("Filtros Productos: " + String.join(", ", productosSeleccionados));
        resultadoArea.setText("Filtros Empleados: " + String.join(", ", empleadosSeleccionados));
        resultadoArea.setText("Filtros Ventas: " + String.join(", ", ventasSeleccionados));

        String sql = "";
        List<TablaConEtiqueta> tablasGeneradas = new ArrayList<>();

        if (!productosSeleccionados.isEmpty()) {
            sql = "SELECT " + String.join(", ", productosSeleccionados) + " FROM Productos";
            tablasGeneradas.add(new TablaConEtiqueta(mostrarDatos(sql), "productos"));
        }
        if (!empleadosSeleccionados.isEmpty()) {
            sql = "SELECT " + String.join(", ", empleadosSeleccionados) + " FROM Empleados";
            tablasGeneradas.add(new TablaConEtiqueta(mostrarDatos(sql), "empleados"));
        }
        if (!ventasSeleccionados.isEmpty()) {
            sql = "SELECT " + String.join(", ", ventasSeleccionados) + " FROM Ventas";
            tablasGeneradas.add(new TablaConEtiqueta(mostrarDatos(sql), "ventas"));
        }
        return tablasGeneradas;
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

    public class TablaConEtiqueta {
        private JTable tabla;
        private String etiqueta;

        public TablaConEtiqueta(JTable tabla, String etiqueta) {
            this.tabla = tabla;
            this.etiqueta = etiqueta;
        }

        public JTable getTabla() {
            return tabla;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

}
