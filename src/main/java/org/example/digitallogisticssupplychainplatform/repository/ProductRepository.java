
package org.example.digitallogisticssupplychainplatform.repository;

import org.example.digitallogisticssupplychainplatform.entity.InventoryMovement;
import org.example.digitallogisticssupplychainplatform.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);
    List<Product> findByCategory(String category);
    boolean existsByCode(String code);
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
    List<Product> findAllActive();

    @Query("SELECT COUNT(sol) FROM SalesOrderLine sol WHERE sol.product = :id")
    Long countBySalesProductId(@Param("productId") Long id);

    @Query(value = "SELECT so.*, sol.* FROM sales_orders so " +
            "LEFT JOIN sales_order_lines sol ON so.id = sol.sales_order_id " +
            "WHERE so.reserved_at IS NULL and where sol.product=:id",
            nativeQuery = true)
    boolean findProductReserved(@Param("id") Long id);
}