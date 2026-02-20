package com.sebastiangelves.ss.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sebastiangelves.ss.model.Usuario;
import com.sebastiangelves.ss.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Si no hay ningún usuario en la base de datos, creamos uno por defecto
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            // Encriptamos la contraseña antes de guardarla
            admin.setPassword(passwordEncoder.encode("admin123")); 
            
            usuarioRepository.save(admin);
            System.out.println("=================================================");
            System.out.println("✅ USUARIO CREADO AUTOMÁTICAMENTE PARA PRUEBAS");
            System.out.println("   -> Usuario: admin");
            System.out.println("   -> Contraseña: admin123");
            System.out.println("=================================================");
        }
    }
}