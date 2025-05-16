package com.billerby.roadvault.repository;

import com.billerby.roadvault.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    
    List<Billing> findByYear(Integer year);
    
    List<Billing> findByYearAndType(Integer year, Billing.BillingType type);
    
    @Query("SELECT b FROM Billing b LEFT JOIN FETCH b.invoices WHERE b.id = :id")
    Billing findByIdWithInvoices(Long id);
}
