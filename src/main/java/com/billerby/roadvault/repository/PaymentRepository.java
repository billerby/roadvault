package com.billerby.roadvault.repository;

import com.billerby.roadvault.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByInvoiceId(Long invoiceId);
    
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Payment> findByPaymentType(Payment.PaymentType paymentType);
}
