package com.example;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.itextpdf.text.Image;

// Asegúrate de importar de java.awt.Dimension

public class GraficosController {

    public DatabaseConnection dbConnection;

    public GraficosController() {
        // Asegúrate de inicializar la conexión a la base de datos
        dbConnection = new DatabaseConnection();
    }

    public Image crearImagenDesdeGrafico(JFreeChart chart) {
        int width = 500;
        int height = 300;

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        chart.draw(graphics2D, new java.awt.geom.Rectangle2D.Double(0, 0, width, height));
        graphics2D.dispose();

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            return Image.getInstance(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JFreeChart crearGraficoVentasPorProducto() throws SQLException {
        String sql = "SELECT Productos.nombre, SUM(Ventas.cantidad) FROM Ventas " +
                "JOIN Productos ON Ventas.id_producto = Productos.id_producto " +
                "GROUP BY Productos.nombre";
        ResultSet resultSet = dbConnection.executeQuery(sql);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            while (resultSet.next()) {
                String producto = resultSet.getString("nombre");
                int cantidadVendida = resultSet.getInt("SUM(Ventas.cantidad)");
                dataset.addValue(cantidadVendida, "Ventas", producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Ventas por Producto",
                "Producto",
                "Cantidad Vendida",
                dataset);

        return barChart;
    }

    public JFreeChart crearGraficoVentasPorEmpleado() throws SQLException {
        String sql = "SELECT Empleados.nombre, SUM(Ventas.total_venta) FROM Ventas " +
                "JOIN Empleados ON Ventas.id_empleado = Empleados.id_empleado " +
                "GROUP BY Empleados.nombre";
        ResultSet resultSet = dbConnection.executeQuery(sql);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            while (resultSet.next()) {
                String empleado = resultSet.getString("nombre");
                double totalVentas = resultSet.getDouble("SUM(Ventas.total_venta)");
                dataset.addValue(totalVentas, "Ventas Totales", empleado);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Ventas Totales por Empleado",
                "Empleado",
                "Ventas Totales",
                dataset);

        return barChart;
    }
}
