package linktic_store.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

// Entidad JPA mapeada a la tabla "users". Implementa UserDetails para integrarse
// directamente con Spring Security sin necesidad de una clase adaptadora aparte.
@Entity
@Table(name = "users")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // Siempre se almacena hasheado con BCrypt, nunca en texto plano.
    @Column(nullable = false)
    private String password;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Purchase> purchases = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Sin roles por ahora; retorna lista vacía de permisos.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    // Métodos requeridos por UserDetails. La cuenta siempre está activa y habilitada.
    // getUsername() retorna el email porque Spring Security lo usa como identificador único.
    @Override public String getPassword()               { return password; }
    @Override public String getUsername()               { return email; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return true; }

    public Long getId()                     { return id; }
    public String getName()                 { return name; }
    public String getEmail()                { return email; }
    public void setName(String u)                       { this.name = u; }
    public void setEmail(String e)                      { this.email = e; }
    public void setPassword(String p)                   { this.password = p; }

    public List<Purchase> getPurchases()                { return purchases; }
    public void setPurchases(List<Purchase> purchases)  { this.purchases = purchases; }
    public void setUpdatedAt(Instant updatedAt)         { this.updatedAt = updatedAt; }
}
