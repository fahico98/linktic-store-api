package linktic_store.repository;

import linktic_store.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Purchase p JOIN FETCH p.products pp JOIN FETCH pp.product WHERE p.id IN :ids ORDER BY p.createdAt DESC")
    List<Purchase> findByIdsWithProducts(@Param("ids") List<Long> ids);
}
