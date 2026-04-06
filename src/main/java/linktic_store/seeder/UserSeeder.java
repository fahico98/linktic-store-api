package linktic_store.seeder;

import linktic_store.model.User;
import linktic_store.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@Order(1)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        List<String[]> seedData = List.of(
                new String[]{"Carlos Mendez",   "carlos.mendez@email.com",   "12345678"},
                new String[]{"Laura Torres",    "laura.torres@email.com",    "12345678"},
                new String[]{"Andres Rojas",    "andres.rojas@email.com",    "12345678"},
                new String[]{"Sofia Vargas",    "sofia.vargas@email.com",    "12345678"},
                new String[]{"Miguel Castillo", "miguel.castillo@email.com", "12345678"}
        );

        for (String[] data : seedData) {
            User user = new User();
            user.setName(data[0]);
            user.setEmail(data[1]);
            user.setPassword(passwordEncoder.encode(data[2]));
            userRepository.save(user);
        }

        System.out.println("UserSeeder: 5 usuarios sembrados correctamente.");
    }
}
