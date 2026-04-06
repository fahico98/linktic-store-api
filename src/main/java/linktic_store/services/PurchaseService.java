package linktic_store.services;

import jakarta.transaction.Transactional;
import linktic_store.model.Product;
import linktic_store.model.Purchase;
import linktic_store.model.PurchaseProduct;
import linktic_store.model.User;
import linktic_store.repository.ProductRepository;
import linktic_store.repository.PurchaseRepository;
import linktic_store.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    private static final int DEFAULT_PER_PAGE = 10;

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Purchase createPurchase(List<PurchaseItem> items, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseProduct> purchaseProducts = new ArrayList<>();

        for (PurchaseItem item : items) {
            Product product = productRepository.findById(item.product().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Product not found: " + item.product().getId()));

            if (product.getAvailableStock() < item.quantity()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Insufficient stock for product: " + product.getName());
            }

            product.setAvailableStock(product.getAvailableStock() - item.quantity());
            productRepository.save(product);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
            purchaseProducts.add(new PurchaseProduct(null, product, item.quantity()));
        }

        Purchase purchase = new Purchase();
        purchase.setPrice(total);
        purchase.setUser(user);

        purchaseProducts.forEach(pp -> pp.setPurchase(purchase));
        purchase.setProducts(purchaseProducts);

        return purchaseRepository.save(purchase);
    }

    public Page<Purchase> getPurchasesByUser(Long userId, int page, Integer perPage) {
        int size = (perPage != null && perPage > 0) ? perPage : DEFAULT_PER_PAGE;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Purchase> purchasePage = purchaseRepository.findByUserId(userId, pageRequest);

        List<Long> ids = purchasePage.getContent().stream().map(Purchase::getId).toList();
        List<Purchase> purchasesWithProducts = purchaseRepository.findByIdsWithProducts(ids);

        return new PageImpl<>(purchasesWithProducts, pageRequest, purchasePage.getTotalElements());
    }

    public record PurchaseItem(Product product, int quantity) {}
}
