package linktic_store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseProduct> products;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId()                                         { return id; }
    public BigDecimal getPrice()                                { return price; }
    public List<PurchaseProduct> getProducts()                  { return products; }
    public Instant getCreatedAt()                               { return createdAt; }

    public void setPrice(BigDecimal price)                      { this.price = price; }
    public void setProducts(List<PurchaseProduct> products)     { this.products = products; }

    public User getUser()                                       { return user; }
    public void setUser(User user)                              { this.user = user; }
}
