package com.sebastiangelves.ss.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sebastiangelves.ss.model.DetalleVenta;
import com.sebastiangelves.ss.model.Producto;
import com.sebastiangelves.ss.model.Venta;
import com.sebastiangelves.ss.repository.VentaRepository;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

  @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private com.sebastiangelves.ss.repository.ProductoRepository productoRepository;

    @Autowired
    private com.sebastiangelves.ss.repository.ClienteRepository clienteRepository;

    @GetMapping
    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearVenta(@RequestBody Venta nuevaVenta) {
        try {
            nuevaVenta.setFecha(new Date());

            for (DetalleVenta detalle : nuevaVenta.getDetalles()) {
                Producto productoDB = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                if (productoDB.getCantidadStock() < detalle.getCantidad()) {
                    return ResponseEntity.badRequest().body("Stock insuficiente para: " + productoDB.getNombre());
                }

                productoDB.setCantidadStock(productoDB.getCantidadStock() - detalle.getCantidad());
                productoRepository.save(productoDB);

                detalle.setVenta(nuevaVenta);
                detalle.setPrecioUnitario(productoDB.getPrecio());
                detalle.setSubtotal(productoDB.getPrecio() * detalle.getCantidad());
            }

            Venta guardada = ventaRepository.save(nuevaVenta);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error técnico: " + e.getMessage());
        }
    }// --- ANULAR VENTA (AUDITORÍA) ---
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> anularVenta(@org.springframework.web.bind.annotation.PathVariable Long id) {
        java.util.Optional<Venta> ventaOpt = ventaRepository.findById(id);
        
        if (ventaOpt.isPresent()) {
            Venta venta = ventaOpt.get();

            // 1. DEVOLVER EL STOCK AL INVENTARIO
            for (com.sebastiangelves.ss.model.DetalleVenta detalle : venta.getDetalles()) {
                com.sebastiangelves.ss.model.Producto producto = detalle.getProducto();
                if (producto != null) {
                    // Le sumamos la cantidad que el cliente había comprado
producto.setCantidadStock(producto.getCantidadStock() + detalle.getCantidad());                    productoRepository.save(producto);
                }
            }

            // 2. RESTAR LOS PUNTOS DE FIDELIZACIÓN AL CLIENTE
            if (venta.getClienteCedula() != null && !venta.getClienteCedula().isEmpty()) {
                java.util.Optional<com.sebastiangelves.ss.model.Cliente> clienteOpt = clienteRepository.findByCedula(venta.getClienteCedula());
                if (clienteOpt.isPresent()) {
                    com.sebastiangelves.ss.model.Cliente cliente = clienteOpt.get();
                    int puntosARestar = (int) (venta.getTotal() / 10000);
                    // Aseguramos que los puntos no queden en negativo
                    cliente.setPuntos(Math.max(0, cliente.getPuntos() - puntosARestar));
                    clienteRepository.save(cliente);
                }
            }

            // 3. FINALMENTE, DESTRUIR LA FACTURA
            ventaRepository.deleteById(id);
            return org.springframework.http.ResponseEntity.ok("{\"message\": \"Venta anulada, stock devuelto y puntos descontados.\"}");
        }
        
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body("{\"error\": \"Venta no encontrada\"}");
    }
}