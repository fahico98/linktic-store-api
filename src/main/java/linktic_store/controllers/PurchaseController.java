package linktic_store.controllers;

import linktic_store.model.Product;
import linktic_store.model.Purchase;
import linktic_store.services.PurchaseService;
import linktic_store.services.PurchaseService.PurchaseItem;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * POST /api/purchases — Crea una nueva compra.
     * Recibe un arreglo donde cada elemento contiene "product", "quantity" y "user_id".
     * Calcula el precio total, descuenta el stock de cada producto y asocia
     * los productos a la compra.
     *
     * @return La compra creada con status 201.
     */
    @GetMapping
    public Page<Purchase> index(
            @RequestParam("user_id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer perPage
    ) {
        return purchaseService.getPurchasesByUser(userId, page, perPage);
    }

    @PostMapping
    public ResponseEntity<Purchase> store(@RequestBody PurchaseRequest request) {
        List<PurchaseItem> purchaseItems = request.getItems().stream()
                .map(item -> new PurchaseItem(item.getProduct(), item.getQuantity()))
                .toList();

        Purchase purchase = purchaseService.createPurchase(purchaseItems, request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(purchase);
    }

    static class PurchaseRequestItem {
        private Product product;
        private int quantity;

        public Product getProduct() { return product; }
        public int getQuantity()    { return quantity; }
        public void setProduct(Product product) { this.product = product; }
        public void setQuantity(int quantity)   { this.quantity = quantity; }
    }

    static class PurchaseRequest {
        private List<PurchaseRequestItem> items;
        private Long userId;

        public List<PurchaseRequestItem> getItems() { return items; }
        public Long getUserId()                     { return userId; }
        public void setItems(List<PurchaseRequestItem> items) { this.items = items; }
        public void setUserId(Long userId)                    { this.userId = userId; }
    }
}
