package linktic_store.services;

import linktic_store.model.Product;
import linktic_store.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("created_at", "updated_at");
    private static final String DEFAULT_SORT_FIELD = "updatedAt";
    private static final int DEFAULT_PER_PAGE = 10;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retorna una página de productos con soporte de paginación y ordenamiento.
     *
     * @param page           Número de página (base 0).
     * @param perPage        Cantidad de registros por página; usa DEFAULT_PER_PAGE si es null o <= 0.
     * @param orderBy        Campo de ordenamiento ("created_at" | "updated_at"); usa DEFAULT_SORT_FIELD si es inválido.
     * @param orderDirection Dirección de ordenamiento ("asc" | "desc"); DESC por defecto.
     * @param searchText     Texto para buscar un producto por su nombre.
     * @return Página de productos según los parámetros indicados.
     * @author Fahibram Cárcamo
     */
    public Page<Product> getProducts(int page, Integer perPage, String orderBy, String orderDirection, String searchText) {
        int size = (perPage != null && perPage > 0) ? perPage : DEFAULT_PER_PAGE;

        String sortField = DEFAULT_SORT_FIELD;

        if (orderBy != null && ALLOWED_SORT_FIELDS.contains(orderBy)) {
            // Convertir snake_case a camelCase para que Hibernate mapee correctamente
            sortField = orderBy.equals("created_at") ? "createdAt" : "updatedAt";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(orderDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));

        if (searchText != null && !searchText.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(searchText, pageRequest);
        }

        return productRepository.findAll(pageRequest);
    }
}
