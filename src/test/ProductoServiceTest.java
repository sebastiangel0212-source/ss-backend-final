import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void testGetAllProductos() {
        // Arrange: Preparamos los datos de prueba
        Producto p1 = new Producto();
        p1.setNombre("Producto 1");
        Producto p2 = new Producto();
        p2.setNombre("Producto 2");
        when(productoRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        // Act: Ejecutamos el método a probar
        List<Producto> productos = productoService.getAllProductos();

        // Assert: Verificamos el resultado
        assertEquals(2, productos.size());
        assertEquals("Producto 1", productos.get(0).getNombre());
    }
}