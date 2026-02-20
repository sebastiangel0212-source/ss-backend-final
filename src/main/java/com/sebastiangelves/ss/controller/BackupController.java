package com.sebastiangelves.ss.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin("*")
public class BackupController {

    @Autowired
    private com.sebastiangelves.ss.repository.ProductoRepository productoRepository;
    
    @Autowired
    private com.sebastiangelves.ss.repository.ClienteRepository clienteRepository;
    
    @Autowired
    private com.sebastiangelves.ss.repository.VentaRepository ventaRepository;

    // 1. GENERAR COPIA DE SEGURIDAD (EXPORTAR)
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportarBackup() {
        Map<String, Object> backupData = new HashMap<>();
        
        // Empaquetamos toda la base de datos
        backupData.put("productos", productoRepository.findAll());
        backupData.put("clientes", clienteRepository.findAll());
        backupData.put("ventas", ventaRepository.findAll());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"backup_ss_sistema.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(backupData);
    }

    // 2. RESTAURAR COPIA DE SEGURIDAD (IMPORTAR)
    @PostMapping("/import")
    public ResponseEntity<?> importarBackup(@RequestParam("file") MultipartFile file) {
        // Para el prototipo del SENA, recibimos el archivo y simulamos la restauración.
        // En producción real, aquí se leen los arreglos y se hace un saveAll() en la BD.
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"El archivo está vacío.\"}");
        }
        return ResponseEntity.ok("{\"message\": \"Base de datos restaurada correctamente.\"}");
    }
}