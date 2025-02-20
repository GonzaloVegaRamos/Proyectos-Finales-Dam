package com.example;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private VBox productosVBox;
    
    @FXML
    private VBox empleadosVBox;
    
    @FXML
    private VBox ventasVBox;
    

    
    @FXML
    private void aplicarFiltros(ActionEvent event) {
        // Obtener los filtros seleccionados de cada VBox
        List<String> productosSeleccionados = getSelectedCheckBoxes(productosVBox);
        List<String> empleadosSeleccionados = getSelectedCheckBoxes(empleadosVBox);
        List<String> ventasSeleccionados = getSelectedCheckBoxes(ventasVBox);
    
        // Contar cuántos VBox tienen checkboxes seleccionados
        int vboxSeleccionados = 0;
        if (!productosSeleccionados.isEmpty()) vboxSeleccionados++;
        if (!empleadosSeleccionados.isEmpty()) vboxSeleccionados++;
        if (!ventasSeleccionados.isEmpty()) vboxSeleccionados++;

                // Validación: solo un VBox puede tener checkboxes seleccionados
                if (vboxSeleccionados > 1) {
                    resultadoArea.setText("Error: Solo puedes seleccionar filtros de una categoría a la vez.");
                    return;
                }

        String vboxSelecionado="";
        if (!ventasSeleccionados.isEmpty()) {
            vboxSelecionado ="Ventas";
        }
        if (!empleadosSeleccionados.isEmpty()) {
            vboxSelecionado ="Empleados";
        }
        if (!productosSeleccionados.isEmpty()) {
            vboxSelecionado ="Productos";
        }

        // Mostrar el resultado si solo un VBox tiene checkboxes seleccionados
        List<String> filtrosSeleccionados = new ArrayList<>();
        filtrosSeleccionados.addAll(productosSeleccionados);
        filtrosSeleccionados.addAll(empleadosSeleccionados);
        filtrosSeleccionados.addAll(ventasSeleccionados);
    
        if (filtrosSeleccionados.isEmpty()) {
            resultadoArea.setText("No se ha seleccionado ningún filtro.");
        } else {
            resultadoArea.setText("Filtros aplicados: " + String.join(", ", filtrosSeleccionados));
        }

            switch (vboxSelecionado) {
                case "Ventas":
                    System.out.println("Filtrando por ID Producto...");
                   
                    break;
                case "Empleados":
                    System.out.println("Filtrando por Nombre...");
                    
                    break;
                case "Productos":

                break;
                default:
                    System.out.println("Filtro no reconocido.");
                    break;
                }
   

        // String sql = "SELECT * FROM Ventas";

        // ResultSet resultSet = dbConnection.executeQuery(sql);

        // StringBuilder resultado = new StringBuilder();

        // while (resultSet.next()) { // Iteramos sobre todos los resultados
        //     int id = resultSet.getInt("id_producto");
        //     String nombre = resultSet.getString("nombre");
        //     double precio = resultSet.getDouble("precio");
        //     int stock = resultSet.getInt("stock");
        // }
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

    private DatabaseConnection dbConnection;

    public MainController() {
        dbConnection = new DatabaseConnection();
    }

    @FXML
    private void mostrarProductos() {
        String sql = "SELECT * FROM Productos";
        mostrarDatos(sql);
    }

    @FXML
    private void mostrarEmpleados() {
        String sql = "SELECT * FROM Empleados";
        mostrarDatos(sql);
    }

    @FXML
    private void mostrarVentas() {
        String sql = "SELECT * FROM Ventas";
        mostrarDatos(sql);
    }

   @FXML
    private void sacarpdf() {
        // Obtener el contenido del TextArea
        String contenido = resultadoArea.getText();

        // Verificar si hay contenido para guardar
        if (contenido.isEmpty()) {
            resultadoArea.setText("No hay datos para guardar en el PDF.");
            return;
        }

        // Crear un nuevo documento PDF
        Document document = new Document();

        try {
            // Especificar la ruta y el nombre del archivo PDF
            PdfWriter.getInstance(document, new FileOutputStream("Reporte.pdf"));

            // Abrir el documento
            document.open();

            // Agregar el contenido del TextArea al PDF
            document.add(new Paragraph(contenido));

            // Cerrar el documento
            document.close();
            System.out.println("PDF generado exitosamente: Reporte.pdf");

        } catch (DocumentException e) {
            resultadoArea.setText("Error al generar el PDF: " + e.getMessage());
        } catch (Exception e) {
            resultadoArea.setText("Error inesperado: " + e.getMessage());
        }
    }

   

    private void mostrarDatos(String sql) {
        resultadoArea.clear(); 

        try {
            ResultSet resultSet = dbConnection.executeQuery(sql);

            StringBuilder resultado = new StringBuilder();

            if (sql.contains("Productos")) {
                resultado.append(String.format("%-5s %-20s %-10s %-10s%n", "ID", "Nombre", "Precio", "Stock"));
                resultado.append("---------------------------------------------------\n");
                
                while (resultSet.next()) { // Iteramos sobre todos los resultados
                    int id = resultSet.getInt("id_producto");
                    String nombre = resultSet.getString("nombre");
                    double precio = resultSet.getDouble("precio");
                    int stock = resultSet.getInt("stock");

                    resultado.append(String.format("%-5d %-20s %-10.2f %-10d%n", id, nombre, precio, stock));
                }
            } else if (sql.contains("Empleados")) {
                resultado.append(String.format("%-5s %-20s %-15s %-15s%n", "ID", "Nombre", "Cargo", "Fecha Contratación"));
                resultado.append("---------------------------------------------------------------\n");

                while (resultSet.next()) {
                    int id = resultSet.getInt("id_empleado");
                    String nombre = resultSet.getString("nombre");
                    String cargo = resultSet.getString("cargo");
                    String fechaContratacion = resultSet.getString("fecha_contratacion");

                    resultado.append(String.format("%-5d %-20s %-15s %-15s%n", id, nombre, cargo, fechaContratacion));
                }
            } else if (sql.contains("Ventas")) {
                resultado.append(String.format("%-5s %-10s %-10s %-10s %-15s %-10s%n", 
                    "ID", "ID Emp", "ID Prod", "Cantidad", "Fecha Venta", "Total"));
                resultado.append("------------------------------------------------------------------\n");

                while (resultSet.next()) {
                    int id = resultSet.getInt("id_venta");
                    int idEmpleado = resultSet.getInt("id_empleado");
                    int idProducto = resultSet.getInt("id_producto");
                    int cantidad = resultSet.getInt("cantidad");
                    String fechaVenta = resultSet.getString("fecha_venta");
                    double totalVenta = resultSet.getDouble("total_venta");

                    resultado.append(String.format("%-8d %-15d %-15d %-11d %-15s %10.2f%n", 
                        id, idEmpleado, idProducto, cantidad, fechaVenta, totalVenta));
                }
            }

            resultadoArea.setText(resultado.toString()); 
            dbConnection.closeResultSet(resultSet); 
        } catch (SQLException e) {
            resultadoArea.setText("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

}
