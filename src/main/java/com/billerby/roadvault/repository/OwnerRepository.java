package com.billerby.roadvault.repository;

import com.billerby.roadvault.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    
    Optional<Owner> findByEmail(String email);
    
    List<Owner> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT o FROM Owner o LEFT JOIN FETCH o.properties WHERE o.id = :id")
    Optional<Owner> findByIdWithProperties(Long id);
}
