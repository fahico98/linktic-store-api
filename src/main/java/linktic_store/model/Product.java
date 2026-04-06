package linktic_store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "products")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_stock", nullable = false)
    private Integer availableStock;

    @Column(columnDefinition = "text")
    private String description;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> images;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<PurchaseProduct> purchases;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public Long getId()                          { return id; }
    public String getName()                      { return name; }
    public BigDecimal getPrice()                 { return price; }
    public Integer getAvailableStock()           { return availableStock; }
    public String getDescription()               { return description; }
    public List<String> getImages()              { return images; }

    public void setId(Long id)                   { this.id = id; }
    public void setName(String name)             { this.name = name; }
    public void setPrice(BigDecimal price)       { this.price = price; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public void setDescription(String description) { this.description = description; }
    public void setImages(List<String> images)   { this.images = images; }

    public List<PurchaseProduct> getPurchases()                      { return purchases; }
    public void setPurchases(List<PurchaseProduct> purchases)        { this.purchases = purchases; }
}
