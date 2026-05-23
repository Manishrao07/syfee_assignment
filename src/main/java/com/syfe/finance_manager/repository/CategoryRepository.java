package com.syfe.finance_manager.repository;

import com.syfe.finance_manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find all categories visible to a specific user (defaults + user's custom ones)
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId")
    List<Category> findByDefaultOrUserId(@Param("userId") Long userId);

    // Find category by name visible to a specific user
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user IS NULL OR c.user.id = :userId)")
    Optional<Category> findByNameAndUserOrUserId(@Param("name") String name, @Param("userId") Long userId);

    Optional<Category> findByNameAndUserId(String name, Long userId);
    
    Optional<Category> findByNameAndUserIsNull(String name);

    boolean existsByNameAndUserIsNull(String name);

    boolean existsByNameAndUserId(String name, Long userId);
}
