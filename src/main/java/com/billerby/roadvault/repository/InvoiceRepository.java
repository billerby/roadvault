package com.billerby.roadvault.repository;

import com.billerby.roadvault.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByOcrNumber(String ocrNumber);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByBillingId(Long billingId);
    
    List<Invoice> findByPropertyId(Long propertyId);
    
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    
    @Query("SELECT MAX(CAST(SUBSTRING(i.invoiceNumber, LENGTH(CONCAT(:year, '-')) + 1, LENGTH(i.invoiceNumber)) AS int)) " +
           "FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT(:year, '-%')")
    Integer findMaxInvoiceNumberByYear(@Param("year") String year);
    
    @Query("SELECT i FROM Invoice i JOIN FETCH i.property JOIN FETCH i.billing WHERE i.id = :id")
    Optional<Invoice> findByIdWithDetails(@Param("id") Long id);
}
