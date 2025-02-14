package com.example;

import java.sql.ResultSet;
import java.sql.SQLException;

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

    private void mostrarDatos(String sql) {
        resultadoArea.clear(); // Limpiar el área de texto

        try {
            ResultSet resultSet = dbConnection.executeQuery(sql);

            StringBuilder resultado = new StringBuilder();
            while (resultSet.next()) {
                // Procesar los resultados según la tabla
                if (sql.contains("Productos")) {
                    int id = resultSet.getInt("id_producto");
                    String nombre = resultSet.getString("nombre");
                    double precio = resultSet.getDouble("precio");
                    int stock = resultSet.getInt("stock");

                    resultado.append("ID: ").append(id)
                            .append(", Nombre: ").append(nombre)
                            .append(", Precio: ").append(precio)
                            .append(", Stock: ").append(stock)
                            .append("\n");
                } else if (sql.contains("Empleados")) {
                    int id = resultSet.getInt("id_empleado");
                    String nombre = resultSet.getString("nombre");
                    String cargo = resultSet.getString("cargo");
                    String fechaContratacion = resultSet.getString("fecha_contratacion");

                    resultado.append("ID: ").append(id)
                            .append(", Nombre: ").append(nombre)
                            .append(", Cargo: ").append(cargo)
                            .append(", Fecha de contratación: ").append(fechaContratacion)
                            .append("\n");
                } else if (sql.contains("Ventas")) {
                    int id = resultSet.getInt("id_venta");
                    int idEmpleado = resultSet.getInt("id_empleado");
                    int idProducto = resultSet.getInt("id_producto");
                    int cantidad = resultSet.getInt("cantidad");
                    String fechaVenta = resultSet.getString("fecha_venta");
                    double totalVenta = resultSet.getDouble("total_venta");

                    resultado.append("ID: ").append(id)
                            .append(", ID Empleado: ").append(idEmpleado)
                            .append(", ID Producto: ").append(idProducto)
                            .append(", Cantidad: ").append(cantidad)
                            .append(", Fecha de venta: ").append(fechaVenta)
                            .append(", Total: ").append(totalVenta)
                            .append("\n");
                }
            }

            resultadoArea.setText(resultado.toString()); // Mostrar los datos en el TextArea
            dbConnection.closeResultSet(resultSet); // Cerrar el ResultSet
        } catch (SQLException e) {
            resultadoArea.setText("Error al ejecutar la consulta: " + e.getMessage());
        }
    }
}