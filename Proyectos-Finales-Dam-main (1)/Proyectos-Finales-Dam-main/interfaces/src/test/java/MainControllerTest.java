import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.JTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.DatabaseConnection;
import com.example.MainController;

public class MainControllerTest {

    private MainController controller;
    private DatabaseConnection mockDbConnection;

    @BeforeEach
    public void setUp() {
        mockDbConnection = mock(DatabaseConnection.class);
        controller = new MainController();
        controller.dbConnection = mockDbConnection;
    }

    @Test
    public void testMostrarDatos_DevuelveTablaNoNula() throws SQLException {
        // Arrange
        String sql = "SELECT * FROM Productos";
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockDbConnection.executeQuery(sql)).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(1)).thenReturn("NombreProducto");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn("Producto1");

        // Act
        JTable resultado = controller.mostrarDatos(sql);

        // Assert
        assertNotNull(resultado, "La tabla no debería ser nula");
        assertEquals(1, resultado.getRowCount(), "La tabla debería tener una fila");
        assertEquals("Producto1", resultado.getValueAt(0, 0), "El valor de la celda debería ser 'Producto1'");
    }

}