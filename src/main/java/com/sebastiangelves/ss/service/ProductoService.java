package com.sebastiangelves.ss.service;

import com.sebastiangelves.ss.model.Producto;
import com.sebastiangelves.ss.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Anotación que define esta clase como un servicio de Spring.
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Método para obtener todos los productos.
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    // Método para obtener un producto por su ID.
    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    // Método para crear un nuevo producto.
    public Producto createProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // Método para actualizar un producto existente.
    public Producto updateProducto(Long id, Producto productoDetails) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        producto.setNombre(productoDetails.getNombre());
        producto.setDescripcion(productoDetails.getDescripcion());
        producto.setPrecio(productoDetails.getPrecio());
        producto.setCantidadStock(productoDetails.getCantidadStock());

        return productoRepository.save(producto);
    }

    // Método para eliminar un producto.
    public void deleteProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        productoRepository.delete(producto);
    }
}