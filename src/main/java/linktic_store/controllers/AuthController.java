package linktic_store.controllers;

import linktic_store.model.User;
import linktic_store.repository.UserRepository;
import linktic_store.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Expone los endpoints públicos de autenticación: login, logout, registro y perfil.
 *
 * @author Fahibram Cárcamo
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * POST /api/auth/login — Valida las credenciales del usuario y retorna un token JWT.
     * Si las credenciales son incorrectas, AuthenticationManager lanza BadCredentialsException (→ 401).
     *
     * @param req Credenciales del usuario (email y password).
     * @return ResponseEntity<?> con el token JWT y su tiempo de expiración.
     * @author Fahibram Cárcamo
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new TokenResponse(token, jwtService.getExpirationMs()));
    }

    /**
     * POST /api/auth/logout — Cierra la sesión del usuario autenticado.
     * La API es stateless (JWT), por lo que la invalidación real del token ocurre en el cliente.
     * Este endpoint existe para que el frontend tenga un contrato explícito de logout.
     *
     * @return ResponseEntity<?> con el mensaje de exito del cierre de sesión.
     * @author Fahibram Cárcamo
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * GET /api/auth/me — Retorna los datos del usuario autenticado, o null si no hay sesión.
     *
     * @param user Usuario autenticado inyectado por Spring Security desde el token JWT.
     * @return ResponseEntity<?> con los datos del usuario, o null si no está autenticado.
     * @author Fahibram Cárcamo
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(new MeResponse(user.getId(), user.getName(), user.getEmail()));
    }

    /**
     * POST /api/auth/register — Crea un nuevo usuario con la contraseña hasheada.
     *
     * @param request Datos del nuevo usuario (name, email y password).
     * @return ResponseEntity<?> con mensaje de éxito, o 400 si el nombre ya está en uso.
     * @author Fahibram Cárcamo
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByName(request.name()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name already taken"));
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // Nunca guardar en texto plano
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new TokenResponse(token, jwtService.getExpirationMs()));
    }

    // Records de Java usados como DTOs ligeros para request/response sin clases extra.
    record LoginRequest(String email, String password) {}
    record RegisterRequest(String name, String email, String password) {}
    record TokenResponse(String token, long expiresIn) {}
    record MeResponse(Long id, String name, String email) {}
}
