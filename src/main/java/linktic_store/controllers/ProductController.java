package linktic_store.controllers;

import linktic_store.model.Product;
import linktic_store.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<Product> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer perPage,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection,
            @RequestParam(required = false) String searchText
    ) {
        return productService.getProducts(page, perPage, orderBy, orderDirection, searchText);
    }
}
