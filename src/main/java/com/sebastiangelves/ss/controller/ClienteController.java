package com.sebastiangelves.ss.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sebastiangelves.ss.model.Cliente;
import com.sebastiangelves.ss.repository.ClienteRepository;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @GetMapping("/{cedula}")
    public ResponseEntity<Cliente> obtenerPorCedula(@PathVariable String cedula) {
        return clienteRepository.findByCedula(cedula)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cliente guardar(@RequestBody Cliente cliente) {
        // Si el cliente ya existe por cédula, lo actualizamos, si no, creamos uno nuevo.
        return clienteRepository.findByCedula(cliente.getCedula())
                .map(existente -> {
                    existente.setNombre(cliente.getNombre());
                    existente.setTelefono(cliente.getTelefono());
                    existente.setEmail(cliente.getEmail());
                    existente.setPuntos(cliente.getPuntos());
                    return clienteRepository.save(existente);
                })
                .orElseGet(() -> clienteRepository.save(cliente));
    }
}
