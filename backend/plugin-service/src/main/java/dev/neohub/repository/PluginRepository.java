package dev.neohub.repository;

import dev.neohub.entity.Plugin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PluginRepository extends JpaRepository<Plugin, UUID> {

    Optional<Plugin> findBySlug(String slug);

    Page<Plugin> findByIsActiveTrue(Pageable pageable);

    Page<Plugin> findByCategory_SlugAndIsActiveTrue(String categorySlug, Pageable pageable);

    Page<Plugin> findByIsColorschemeTrue(Pageable pageable);

    // Full text search using PostgreSQL tsvector
    @Query(value = """
            SELECT * FROM plugins
            WHERE is_active = true
            AND search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
            """, nativeQuery = true)
    Page<Plugin> searchPlugins(@Param("query") String query, Pageable pageable);

    // Filter by category + search combined
    @Query(value = """
            SELECT p.* FROM plugins p
            JOIN categories c ON p.category_id = c.id
            WHERE p.is_active = true
            AND (:categorySlug IS NULL OR c.slug = :categorySlug)
            AND (:query IS NULL OR p.search_vector @@ plainto_tsquery('english', :query))
            ORDER BY p.github_stars DESC
            """, nativeQuery = true)
    Page<Plugin> filterPlugins(
            @Param("query") String query,
            @Param("categorySlug") String categorySlug,
            Pageable pageable);
}