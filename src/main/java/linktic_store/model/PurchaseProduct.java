package linktic_store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;

@Entity
@Table(name = "purchase_products")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PurchaseProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public PurchaseProduct() {}

    public PurchaseProduct(Purchase purchase, Product product, Integer quantity) {
        this.purchase = purchase;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId()                      { return id; }
    public Purchase getPurchase()            { return purchase; }
    public Product getProduct()              { return product; }
    public Integer getQuantity()             { return quantity; }

    public void setPurchase(Purchase purchase) { this.purchase = purchase; }
    public void setProduct(Product product)    { this.product = product; }
    public void setQuantity(Integer quantity)  { this.quantity = quantity; }
}
