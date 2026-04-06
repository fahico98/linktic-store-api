package linktic_store.seeder;

import jakarta.transaction.Transactional;
import linktic_store.model.Product;
import linktic_store.model.Purchase;
import linktic_store.model.PurchaseProduct;
import linktic_store.model.User;
import linktic_store.repository.ProductRepository;
import linktic_store.repository.PurchaseRepository;
import linktic_store.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
@Order(3)
public class PurchaseSeeder implements CommandLineRunner {

    private static final int MIN_PURCHASES = 15;
    private static final int MAX_PURCHASES = 30;
    private static final int MAX_PRODUCTS_PER_PURCHASE = 5;
    private static final int[] QUANTITY_OPTIONS = {1, 2, 5, 10, 12};

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public PurchaseSeeder(PurchaseRepository purchaseRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (purchaseRepository.count() > 0) return;

        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();

        if (users.isEmpty() || products.isEmpty()) {
            System.out.println("PurchaseSeeder: no hay usuarios o productos disponibles.");
            return;
        }

        Random random = new Random(42);
        int totalPurchases = 0;

        for (User user : users) {
            if (user.getId() == 1) continue;

            int numPurchases = MIN_PURCHASES + random.nextInt(MAX_PURCHASES - MIN_PURCHASES + 1);

            for (int i = 0; i < numPurchases; i++) {
                int numProducts = 1 + random.nextInt(MAX_PRODUCTS_PER_PURCHASE);

                List<Product> shuffled = new ArrayList<>(products);
                Collections.shuffle(shuffled, random);
                List<Product> selectedProducts = shuffled.subList(0, Math.min(numProducts, shuffled.size()));

                Purchase purchase = new Purchase();
                purchase.setUser(user);

                List<PurchaseProduct> purchaseProducts = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (Product product : selectedProducts) {
                    int quantity = QUANTITY_OPTIONS[random.nextInt(QUANTITY_OPTIONS.length)];
                    total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                    purchaseProducts.add(new PurchaseProduct(purchase, product, quantity));
                }

                purchase.setPrice(total);
                purchase.setProducts(purchaseProducts);
                purchaseRepository.save(purchase);
            }

            totalPurchases += numPurchases;
        }

        System.out.println("PurchaseSeeder: " + totalPurchases + " compras sembradas para " + users.size() + " usuarios.");
    }
}
