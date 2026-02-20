package com.sebastiangelves.ss.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sebastiangelves.ss.model.Usuario;
import com.sebastiangelves.ss.repository.UsuarioRepository;
import com.sebastiangelves.ss.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // EL NUEVO CARTERO DE SPRING BOOT
    @Autowired
    private JavaMailSender mailSender;

    // 1. LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent() && passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Credenciales inválidas\"}");
        }
    }

    // 2. RECUPERAR CLAVE (Envío de OTP por Correo)
    @PostMapping("/recover")
    public ResponseEntity<?> recoverPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Verificamos que el usuario tenga un correo registrado
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Este usuario no tiene un correo configurado. Contacta al administrador.\"}");
            }

            // Generamos un código aleatorio de 6 dígitos
            String otp = String.format("%06d", new Random().nextInt(999999));
            
            // Lo guardamos en la base de datos temporalmente
            usuario.setCodigoRecuperacion(otp);
            usuarioRepository.save(usuario);

            // Redactamos y enviamos el correo
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(usuario.getEmail());
                message.setSubject("Código de Recuperación - S&S Inventario");
                message.setText("Hola " + usuario.getUsername() + ",\n\n"
                        + "Has solicitado recuperar tu contraseña.\n"
                        + "Tu código de seguridad temporal es: " + otp + "\n\n"
                        + "Ingresa este código en el sistema para crear una nueva contraseña.\n"
                        + "Si no solicitaste esto, ignora este mensaje.");
                
                mailSender.send(message);
                
                // Le devolvemos al frontend una pista del correo (ej. j***@gmail.com)
                String correoOculto = usuario.getEmail().replaceAll("(^[^@]{2}|(?!^)\\G)[^@]", "$1*");
                return ResponseEntity.ok("{\"message\": \"Código enviado a " + correoOculto + "\"}");

            // --- AQUÍ APLICAMOS LA CORRECCIÓN ELEGANTE QUE PEDÍA VS CODE ---
            } catch (org.springframework.mail.MailException e) {
                System.err.println("Error al enviar el correo: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"Error al conectar con el servidor de correos.\"}");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Usuario no encontrado\"}");
        }
    }

    // 3. VALIDAR OTP Y CAMBIAR CONTRASEÑA
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Verificamos si el código coincide
            if (usuario.getCodigoRecuperacion() != null && usuario.getCodigoRecuperacion().equals(otp)) {
                
                // Encriptamos la nueva clave y borramos el código (para que no se pueda reusar)
                usuario.setPassword(passwordEncoder.encode(newPassword));
                usuario.setCodigoRecuperacion(null); 
                usuarioRepository.save(usuario);

                return ResponseEntity.ok("{\"message\": \"Contraseña actualizada con éxito\"}");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Código incorrecto o expirado\"}");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Usuario no encontrado\"}");
    }
    // 4. EDITAR USUARIO (Actualizar correo, teléfono, foto, etc.)
    @PostMapping("/update/{id}")
    public ResponseEntity<?> actualizarUsuario(@org.springframework.web.bind.annotation.PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Actualizamos solo los datos que nos envíe el frontend
            if (request.containsKey("username")) usuario.setUsername(request.get("username"));
            if (request.containsKey("email")) usuario.setEmail(request.get("email"));
            if (request.containsKey("telefono")) usuario.setTelefono(request.get("telefono"));
            if (request.containsKey("fotoPerfil")) usuario.setFotoPerfil(request.get("fotoPerfil"));
            
            // Si el admin decide cambiarle la contraseña también
            if (request.containsKey("password") && !request.get("password").isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(request.get("password")));
            }

            usuarioRepository.save(usuario);
            return ResponseEntity.ok("{\"message\": \"Usuario actualizado con éxito\"}");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Usuario no encontrado\"}");
        }
    }
    // 5. REGISTRAR NUEVO USUARIO (Con correo)
    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        // Verificamos si el nombre ya está en uso
        if (usuarioRepository.findByUsername(nuevoUsuario.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"El nombre de usuario ya existe.\"}");
        }

        // Encriptamos la clave y guardamos
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));
        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.ok("{\"message\": \"Usuario registrado con éxito\"}");
    }

    // 6. LISTAR TODOS LOS USUARIOS (Para la tabla)
    @GetMapping("/lista")
    public ResponseEntity<?> listarUsuarios() {
        // Retorna todos los usuarios de la base de datos
        return ResponseEntity.ok(usuarioRepository.findAll());
    }
    // 7. ELIMINAR USUARIO (Dar de baja a un empleado)
    @org.springframework.web.bind.annotation.DeleteMapping("/delete/{id}")
    public ResponseEntity<?> eliminarUsuario(@org.springframework.web.bind.annotation.PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // REGLA DE ORO: ¡Nadie puede borrar al dios del sistema!
            if (usuario.getUsername().equalsIgnoreCase("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"¡Operación denegada! No puedes eliminar al administrador principal del sistema.\"}");
            }
            
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok("{\"message\": \"Empleado dado de baja con éxito\"}");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Usuario no encontrado\"}");
        }
    }
}