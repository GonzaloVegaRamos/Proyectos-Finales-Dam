import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.example.DatabaseConnection;
import com.example.MainController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class MainControllerTest {

    private MainController mainController;

    @Mock
    private VBox productosVBox;
    @Mock
    private VBox empleadosVBox;
    @Mock
    private VBox ventasVBox;

    @Mock
    private CheckBox checkBoxProducto1;
    @Mock
    private CheckBox checkBoxProducto2;
    @Mock
    private CheckBox checkBoxEmpleado1;
    @Mock
    private CheckBox checkBoxEmpleado2;

    // Mock de la clase DatabaseConnection
    @Mock
    private DatabaseConnection dbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        Platform.startup(() -> {}); 

        MockitoAnnotations.openMocks(this);  // Inicializa los mocks

        // Crear el controlador y asignar los mocks
        mainController = new MainController();
        mainController.dbConnection = dbConnection;  // Asignar el mock de la base de datos

        // Crear una ObservableList de los checkboxes
        ObservableList<Node> productosChildren = FXCollections.observableArrayList(checkBoxProducto1, checkBoxProducto2);
        ObservableList<Node> empleadosChildren = FXCollections.observableArrayList(checkBoxEmpleado1, checkBoxEmpleado2);

        // Configurar los VBox con los checkboxes mockeados
        when(productosVBox.getChildren()).thenReturn(productosChildren);
        when(empleadosVBox.getChildren()).thenReturn(empleadosChildren);

        // Mock de las consultas a la base de datos con un ResultSet ficticio
ResultSet mockResultSet = mock(ResultSet.class);

// Configura lo que debe devolver el mock
when(dbConnection.executeQuery("SELECT Producto1, Producto2 FROM Productos")).thenReturn(mockResultSet);
when(dbConnection.executeQuery("SELECT Empleado1, Empleado2 FROM Empleados")).thenReturn(mockResultSet);
when(dbConnection.executeQuery("SELECT Venta1, Venta2 FROM Ventas")).thenReturn(mockResultSet);

    }

    // Crear un mock del ResultSet
    private ResultSet mockResultSet() {
        ResultSet resultSet = mock(ResultSet.class);

        try {
            // Simula que el ResultSet tiene dos filas
            when(resultSet.next()).thenAnswer(new Answer<Boolean>() {
                private int count = 0;

                @Override
                public Boolean answer(org.mockito.invocation.InvocationOnMock invocation) {
                    return count++ < 2;  // Devuelve 'true' dos veces (dos filas)
                }
            });

            // Simula que el ResultSet devuelve los valores esperados para las columnas
            when(resultSet.getObject(1)).thenReturn("Valor1");
            when(resultSet.getObject(2)).thenReturn("Valor2");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    @Test
    void testAplicarFiltrosCuandoTodosCheckboxEstanMarcados() {
        // Configurar los checkboxes para que todos estén seleccionados
        when(checkBoxProducto1.isSelected()).thenReturn(true);
        when(checkBoxProducto2.isSelected()).thenReturn(true);
        when(checkBoxEmpleado1.isSelected()).thenReturn(true);
        when(checkBoxEmpleado2.isSelected()).thenReturn(true);

        // Llamar al método aplicarFiltros
        List<JTable> tablasGeneradas = mainController.aplicarFiltros();

        // Verificar que la lista de tablas generadas contiene 3 tablas (productos, empleados y ventas)
        assertEquals(3, tablasGeneradas.size(), "Debe haber tres tablas generadas.");
    }

    @Test
    void testMarcarCheckBoxes() {
        // Configurar los checkboxes para el caso en el que todos estén marcados
        when(checkBoxProducto1.isSelected()).thenReturn(true);
        when(checkBoxProducto2.isSelected()).thenReturn(true);

        // Invocar el método marcarCheckBoxes en el VBox de productos
        mainController.marcarCheckBoxesEnVBox(productosVBox);

        // Verificar que todos los checkboxes se desmarcaron (debido a que ya estaban marcados)
        verify(checkBoxProducto1).setSelected(false);
        verify(checkBoxProducto2).setSelected(false);
    }

    @Test
    void testMarcarCheckBoxesCuandoHayCheckboxDesmarcado() {
        // Configurar un checkbox desmarcado
        when(checkBoxProducto1.isSelected()).thenReturn(false);
        when(checkBoxProducto2.isSelected()).thenReturn(true);

        // Invocar el método marcarCheckBoxes en el VBox de productos
        mainController.marcarCheckBoxesEnVBox(productosVBox);

        // Verificar que ambos checkboxes se marcaron
        verify(checkBoxProducto1).setSelected(true);
        verify(checkBoxProducto2).setSelected(true);
    }
}
