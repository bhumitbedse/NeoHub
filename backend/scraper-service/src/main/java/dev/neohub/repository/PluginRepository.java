package dev.neohub.repository;

import dev.neohub.entity.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PluginRepository extends JpaRepository<Plugin, UUID> {

    @Query("SELECT p FROM Plugin p WHERE p.isActive = true " +
            "ORDER BY p.lastScrapedAt ASC NULLS FIRST")
    List<Plugin> findAllActiveOrderByLastScrapedAsc();
}