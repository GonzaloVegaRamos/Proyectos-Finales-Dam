import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import com.example.DatabaseConnection;

public class DatabaseConnectionTest {

    private DatabaseConnection dbConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializar los mocks
        dbConnection = new DatabaseConnection();

    }

    // Prueba para getConnection()
    @Test
    public void testGetConnection() throws SQLException {
        // Ejecutar el método
        Connection connection = dbConnection.getConnection();

        // Verificar que la conexión no es nula
        assertNotNull(connection, "La conexión no debería ser nula");

        // Verificar que la conexión es válida
        assertFalse(connection.isClosed(), "La conexión debería estar abierta");

        // Cerrar la conexión
        dbConnection.closeConnection(connection);
    }

    // Prueba para closeConnection(Connection connection)
    @Test
    public void testCloseConnection() throws SQLException {
        // Ejecutar el método
        dbConnection.closeConnection(mockConnection);

        // Verificar que se llamó al método close()
        verify(mockConnection).close();
    }

    // Prueba para closeResultSet(ResultSet resultSet)
    @Test
    public void testCloseResultSet() throws SQLException {
        // Ejecutar el método
        dbConnection.closeResultSet(mockResultSet);

        // Verificar que se llamó al método close()
        verify(mockResultSet).close();
    }

    // Prueba para closeStatement(PreparedStatement statement)
    @Test
    public void testCloseStatement() throws SQLException {
        // Ejecutar el método
        dbConnection.closeStatement(mockStatement);

        // Verificar que se llamó al método close()
        verify(mockStatement).close();
    }
}