import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.framework.junit5.ApplicationTest;

import com.example.DatabaseConnection;
import com.example.MainController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainControllerTest extends ApplicationTest {

    private MainController controller;
    private DatabaseConnection mockDbConnection;

    @Override
    public void start(Stage stage) throws IOException {
        // Aquí puedes inicializar tu escena si es necesario
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "MainView.fxml"));
        VBox root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() throws IOException {
        mockDbConnection = mock(DatabaseConnection.class);
        controller = new MainController();
        controller.dbConnection = mockDbConnection;

        // Cargar el FXML para obtener el VBox y sus hijos
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        VBox root = loader.load();
        controller = loader.getController(); // Obtén el controlador directamente del FXMLLoader

        // Asegúrate de que los VBox están configurados correctamente
        controller.productosVBox.getChildren().addAll(
                new CheckBox("Producto 1"),
                new CheckBox("Producto 2"),
                new CheckBox("Producto 3"));
        controller.empleadosVBox.getChildren().addAll(
                new CheckBox("Empleado 1"),
                new CheckBox("Empleado 2"),
                new CheckBox("Empleado 3"));
    }

    @Test
    public void testMostrarDatos_DevuelveTablaNoNula() throws SQLException {
        // Arrange
        String sql = "SELECT * FROM Productos";
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        // Configura el mock para devolver 1 fila
        when(mockDbConnection.executeQuery(sql)).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(1)).thenReturn("NombreProducto");

        // Simula que hay 1 fila en el ResultSet
        when(mockResultSet.next()).thenReturn(true, false); // Una fila y luego false
        when(mockResultSet.getObject(1)).thenReturn("Producto1"); // Valor de la primera fila

        // Act
        JTable resultado = controller.mostrarDatos(sql);

        // Assert
        assertNotNull(resultado, "La tabla no debería ser nula");
        assertTrue(resultado.getRowCount() > 0, "La tabla debería tener más de 0 filas"); // Verifica que hay al menos
                                                                                          // una fila
        assertEquals(1, resultado.getValueAt(0, 0), "El valor de la celda debería ser 'Producto1'");
    }

    private VBox vbox;

    @Test
    public void testMarcarCheckBoxes_CasoTodosDesmarcados() {
        // Dado: todos los CheckBox desmarcados en productosVBox
        controller.marcarCheckBoxesEnVBox(controller.productosVBox);

        // Entonces: todos los CheckBoxes de productos deben estar marcados
        controller.productosVBox.getChildren().forEach(node -> {
            if (node instanceof CheckBox checkBox) {
                assertTrue(checkBox.isSelected(), "El checkbox debería estar marcado");
            }
        });
    }

    @Test
    public void testAplicarFiltros() {
        // Dado: al menos un checkbox marcado en cada VBox
        controller.productosVBox.getChildren().forEach(node -> {
            if (node instanceof CheckBox checkBox) {
                if ("Producto 1".equals(checkBox.getText())) {
                    checkBox.setSelected(true);
                }
            }
        });

        controller.empleadosVBox.getChildren().forEach(node -> {
            if (node instanceof CheckBox checkBox) {
                if ("Empleado 1".equals(checkBox.getText())) {
                    checkBox.setSelected(true);
                }
            }
        });

        // Act: Llamar al método aplicarFiltros
        List<MainController.TablaConEtiqueta> tablas = controller.aplicarFiltros();

        // Assert: Verificar que las tablas devueltas son correctas
        assertNotNull(tablas, "Las tablas no pueden ser null.");
        assertEquals(2, tablas.size(), "Debe haber dos tablas: productos y empleados.");
        assertEquals("productos", tablas.get(0).getEtiqueta(),
                "La etiqueta de la primera tabla debería ser productos.");
        assertEquals("empleados", tablas.get(1).getEtiqueta(),
                "La etiqueta de la segunda tabla debería ser empleados.");
    }

}