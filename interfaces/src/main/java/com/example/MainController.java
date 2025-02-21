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
    private CheckBox Gproducto;

    @FXML
    private CheckBox Gempleado;

    @FXML
    private VBox productosVBox;

    @FXML
    private VBox empleadosVBox;

    @FXML
    private VBox ventasVBox;

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
                // Título para gráfico de productos
                Paragraph tituloGraficoProductos = new Paragraph("Gráfico de Ventas por Producto",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                tituloGraficoProductos.setAlignment(Element.ALIGN_LEFT);
                document.add(tituloGraficoProductos);
                document.add(new Chunk("\n"));

                JFreeChart graficoProductos = graficos.crearGraficoVentasPorProducto();
                Image chartProductos = graficos.crearImagenDesdeGrafico(graficoProductos);
                System.out.println("Imagen generada con éxito. Tamaño: " +
                        chartProductos.getWidth() + "x" + chartProductos.getHeight());
                chartProductos.scaleToFit(500, 300);
                document.add(chartProductos);
                document.add(new Chunk("\n"));
            }

            if (Gempleado.isSelected()) {
                // Título para gráfico de empleados
                Paragraph tituloGraficoEmpleados = new Paragraph("Gráfico de Ventas por Empleado",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                tituloGraficoEmpleados.setAlignment(Element.ALIGN_LEFT);
                document.add(tituloGraficoEmpleados);
                document.add(new Chunk("\n"));

                JFreeChart graficoVentas = graficos.crearGraficoVentasPorEmpleado();
                Image chartEmpleados = graficos.crearImagenDesdeGrafico(graficoVentas);
                System.out.println("Imagen generada con éxito. Tamaño: " +
                        chartEmpleados.getWidth() + "x" + chartEmpleados.getHeight());
                chartEmpleados.scaleToFit(500, 300);
                document.add(chartEmpleados);
                document.add(new Chunk("\n"));
            }

            // Obtener las tablas generadas
            List<JTable> tablas = aplicarFiltros();
            if (tablas != null && !tablas.isEmpty()) {
                // Iterar sobre cada JTable en la lista
                for (JTable tabla : tablas) {

                    // Título de la tabla
                    Paragraph tituloTabla = new Paragraph("Datos de la Tabla",
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                    tituloTabla.setAlignment(Element.ALIGN_LEFT);
                    document.add(tituloTabla);
                    document.add(new Chunk("\n"));

                    // Crear una PdfPTable con el número de columnas de la JTable
                    PdfPTable pdfTable = new PdfPTable(tabla.getColumnCount());
                    pdfTable.setWidthPercentage(100); // Ocupa todo el ancho de la página

                    // Agregar los encabezados de columna con estilo
                    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                    for (int i = 0; i < tabla.getColumnCount(); i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(tabla.getColumnName(i), headerFont));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY); // Fondo gris para los encabezados
                        pdfTable.addCell(cell);
                    }

                    // Agregar las filas de datos
                    for (int i = 0; i < tabla.getRowCount(); i++) {
                        for (int j = 0; j < tabla.getColumnCount(); j++) {
                            PdfPCell cell = new PdfPCell(new Phrase(tabla.getValueAt(i, j).toString()));
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
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Datos");

        // Crear una fila para el título
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Informe de Ventas");

        // Crear un contenedor de gráficos en la hoja
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        // Agregar gráficos si están seleccionados
        if (Gproducto.isSelected()) {
            JFreeChart graficoProductos = graficos.crearGraficoVentasPorProducto();
            com.itextpdf.text.Image chartProductos = graficos.crearImagenDesdeGrafico(graficoProductos);
            System.out.println("Imagen generada con éxito. Tamaño: " + chartProductos.getWidth() + "x"
                    + chartProductos.getHeight());

            // Convertir la imagen de iText a BufferedImage
            BufferedImage bufferedImage = convertirABufferedImage(chartProductos);

            // Agregar la imagen del gráfico al archivo Excel
            byte[] imageBytes = imageToByteArray(bufferedImage);
            int pictureIdx = ((XSSFWorkbook) workbook).addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);

            // Crear un ancla para colocar la imagen
            ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 1, 5, 6); // Parámetros para anclar la imagen a la
                                                                                // celda
            XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
            picture.resize(1.0);

        } else if (Gempleado.isSelected()) {
            JFreeChart graficoVentas = graficos.crearGraficoVentasPorEmpleado();
            com.itextpdf.text.Image chartEmpleados = graficos.crearImagenDesdeGrafico(graficoVentas);
            System.out.println("Imagen generada con éxito. Tamaño: " + chartEmpleados.getWidth() + "x"
                    + chartEmpleados.getHeight());

            // Convertir la imagen de iText a BufferedImage
            BufferedImage bufferedImage = convertirABufferedImage(chartEmpleados);

            // Agregar la imagen del gráfico al archivo Excel
            byte[] imageBytes = imageToByteArray(bufferedImage);
            int pictureIdx = ((XSSFWorkbook) workbook).addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);

            // Crear un ancla para colocar la imagen
            ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 1, 5, 6); // Parámetros para anclar la imagen a la
                                                                                // celda
            XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
            picture.resize(1.0);
        }

        // Obtener las tablas generadas
        List<JTable> tablas = aplicarFiltros();
        if (tablas != null && !tablas.isEmpty()) {
            int rowIndex = 3; // Empezamos desde la fila 3 para las tablas

            // Iterar sobre cada JTable en la lista
            for (JTable tabla : tablas) {
                // Crear una nueva fila para la tabla
                row = sheet.createRow(rowIndex++);
                // Agregar los encabezados de columna
                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    cell = row.createCell(i);
                    cell.setCellValue(tabla.getColumnName(i));
                }

                // Agregar las filas de datos
                for (int i = 0; i < tabla.getRowCount(); i++) {
                    row = sheet.createRow(rowIndex++);
                    for (int j = 0; j < tabla.getColumnCount(); j++) {
                        cell = row.createCell(j);
                        cell.setCellValue(tabla.getValueAt(i, j).toString());
                    }
                }

                // Añadir un salto de línea entre tablas (agregar una fila vacía)
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

    @FXML
    private TextArea resultadoArea;

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

}
