
package org.example.digitallogisticssupplychainplatform.repository;

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
    List<Product> findByActiveTrue();
    List<Product> findByCategory(String category);
    List<Product> findByUserId(Long userId);
    boolean existsByCode(String code);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.user WHERE p.id = :id")
    Optional<Product> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.user WHERE p.status = 'ACTIVE'")
    List<Product> findAllActiveWithUser();
}