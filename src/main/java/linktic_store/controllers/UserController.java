package linktic_store.controllers;

import linktic_store.model.User;
import linktic_store.repository.UserRepository;
import linktic_store.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Expone los endpoints de gestión de usuarios autenticados.
 *
 * @author Fahibram Cárcamo
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * PUT /api/users — Actualiza los datos de un usuario existente.
     *
     * @param request Datos del usuario a actualizar (user_id, name, email y password).
     * @return ResponseEntity<?> con mensaje de éxito, o 404 si el usuario no existe,
     *         o 400 si el nombre ya está en uso por otro usuario.
     * @author Fahibram Cárcamo
     */
    @PutMapping
    public ResponseEntity<?> update(@RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(request.userId()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!user.getName().equals(request.name()) &&
                userRepository.findByName(request.name()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name already taken"));
        }

        user.setName(request.name());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new UpdateUserResponse(user.getId(), user.getName(), user.getEmail(), token));
    }

    record UpdateUserRequest(Long userId, String name, String email, String password) {}
    record UpdateUserResponse(Long id, String name, String email, String token) {}
}
