
package com.example;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class MainController {
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
} 
else if (sql.contains("Ventas")) {
    resultado.append(String.format("%-5s %-12s %-12s %-10s %-15s %-10s%n", 
        "ID", "ID Emp", "ID Prod", "Cantidad", "Fecha Venta", "Total"));
    resultado.append("------------------------------------------------------------------\n");

    while (resultSet.next()) {
        int id = resultSet.getInt("id_venta");
        int idEmpleado = resultSet.getInt("id_empleado");
        int idProducto = resultSet.getInt("id_producto");
        int cantidad = resultSet.getInt("cantidad");
        String fechaVenta = resultSet.getString("fecha_venta");
        double totalVenta = resultSet.getDouble("total_venta");

        resultado.append(String.format("%-8d %-15d %-15d %-13d %-15s %10.2f%n", 
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







