package linktic_store.seeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import linktic_store.model.Product;
import linktic_store.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
@Order(2)
public class ProductSeeder implements CommandLineRunner {

    private static final String API_URL = "https://dummyjson.com/products?limit=50";

    private final ProductRepository productRepository;
    private final RestClient restClient;

    public ProductSeeder(ProductRepository productRepository, RestClient.Builder restClientBuilder) {
        this.productRepository = productRepository;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        ApiResponse response = restClient.get()
                .uri(API_URL)
                .retrieve()
                .body(ApiResponse.class);

        List<ApiProduct> apiProducts = response != null ? response.products() : null;

        if (apiProducts == null || apiProducts.isEmpty()) {
            System.out.println("ProductSeeder: no se obtuvieron productos de la API.");
            return;
        }

        Random random = new Random(42);

        for (ApiProduct apiProduct : apiProducts) {
            // Precio aleatorio entre 10_000 y 1_000_000 COP, en múltiplos de 10_000
            int priceUnits = (random.nextInt(100) + 1) * 10_000;

            Product product = new Product();
            product.setName(apiProduct.title());
            product.setDescription(apiProduct.description());
            product.setPrice(new BigDecimal(priceUnits));
            product.setAvailableStock((random.nextInt(10) + 1) * 10);
            product.setImages(apiProduct.images());
            productRepository.save(product);
        }

        System.out.println("ProductSeeder: " + apiProducts.size() + " productos sembrados correctamente.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiResponse(List<ApiProduct> products) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiProduct(String title, String description, List<String> images) {}
}
