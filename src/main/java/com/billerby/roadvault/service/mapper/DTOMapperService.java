package com.billerby.roadvault.service.mapper;

import com.billerby.roadvault.dto.AssociationDTO;
import com.billerby.roadvault.dto.BillingDTO;
import com.billerby.roadvault.dto.InvoiceDTO;
import com.billerby.roadvault.dto.OwnerDTO;
import com.billerby.roadvault.dto.PaymentDTO;
import com.billerby.roadvault.dto.PropertyDTO;
import com.billerby.roadvault.model.Association;
import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.model.Payment;
import com.billerby.roadvault.model.Property;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for mapping between DTOs and entities.
 */
@Service
public class DTOMapperService {
    
    /**
     * Convert an Invoice entity to an InvoiceDTO.
     *
     * @param invoice The invoice entity
     * @return The converted DTO
     */
    public InvoiceDTO toInvoiceDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        
        if (invoice.getBilling() != null) {
            dto.setBillingId(invoice.getBilling().getId());
            dto.setBillingDescription(invoice.getBilling().getDescription());
        }
        
        if (invoice.getProperty() != null) {
            dto.setPropertyId(invoice.getProperty().getId());
            dto.setPropertyDesignation(invoice.getProperty().getPropertyDesignation());
            
            if (invoice.getProperty().getMainContact() != null) {
                dto.setOwnerName(invoice.getProperty().getMainContact().getName());
            }
        }
        
        dto.setAmount(invoice.getAmount());
        dto.setDueDate(invoice.getDueDate());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setOcrNumber(invoice.getOcrNumber());
        dto.setStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        
        return dto;
    }
    
    /**
     * Convert a list of Invoice entities to a list of InvoiceDTOs.
     *
     * @param invoices The list of invoice entities
     * @return The list of converted DTOs
     */
    public List<InvoiceDTO> toInvoiceDTOList(List<Invoice> invoices) {
        if (invoices == null) {
            return List.of();
        }
        
        return invoices.stream()
                .map(this::toInvoiceDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a Payment entity to a PaymentDTO.
     *
     * @param payment The payment entity
     * @return The converted DTO
     */
    public PaymentDTO toPaymentDTO(Payment payment) {
        if (payment == null) {
            return null;
        }
        
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        
        if (payment.getInvoice() != null) {
            dto.setInvoiceId(payment.getInvoice().getId());
            dto.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
        }
        
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setComment(payment.getComment());
        dto.setPaymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : null);
        
        return dto;
    }
    
    /**
     * Convert a list of Payment entities to a list of PaymentDTOs.
     *
     * @param payments The list of payment entities
     * @return The list of converted DTOs
     */
    public List<PaymentDTO> toPaymentDTOList(List<Payment> payments) {
        if (payments == null) {
            return List.of();
        }
        
        return payments.stream()
                .map(this::toPaymentDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a PaymentDTO to a Payment entity.
     *
     * @param dto The payment DTO
     * @return The converted entity
     */
    public Payment toPaymentEntity(PaymentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setComment(dto.getComment());
        
        if (dto.getPaymentType() != null) {
            try {
                payment.setPaymentType(Payment.PaymentType.valueOf(dto.getPaymentType()));
            } catch (IllegalArgumentException e) {
                // Use default type if invalid
                payment.setPaymentType(Payment.PaymentType.MANUAL);
            }
        } else {
            payment.setPaymentType(Payment.PaymentType.MANUAL);
        }
        
        return payment;
    }
    
    /**
     * Convert an Association entity to an AssociationDTO.
     *
     * @param association The association entity
     * @return The converted DTO
     */
    public AssociationDTO toAssociationDTO(Association association) {
        if (association == null) {
            return null;
        }
        
        AssociationDTO dto = new AssociationDTO();
        dto.setId(association.getId());
        dto.setName(association.getName());
        dto.setOrganizationNumber(association.getOrganizationNumber());
        dto.setBankgiroNumber(association.getBankgiroNumber());
        dto.setPlusgiroNumber(association.getPlusgiroNumber());
        dto.setEmail(association.getEmail());
        dto.setPhone(association.getPhone());
        dto.setAddress(association.getAddress());
        dto.setPostalCode(association.getPostalCode());
        dto.setCity(association.getCity());
        dto.setWebsite(association.getWebsite());
        dto.setInvoiceText(association.getInvoiceText());
        dto.setReminderDays(association.getReminderDays());
        dto.setReminderFee(association.getReminderFee());
        
        return dto;
    }
    
    /**
     * Convert an AssociationDTO to an Association entity.
     *
     * @param dto The association DTO
     * @return The converted entity
     */
    public Association toAssociationEntity(AssociationDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Association association = new Association();
        association.setId(dto.getId());
        association.setName(dto.getName());
        association.setOrganizationNumber(dto.getOrganizationNumber());
        association.setBankgiroNumber(dto.getBankgiroNumber());
        association.setPlusgiroNumber(dto.getPlusgiroNumber());
        association.setEmail(dto.getEmail());
        association.setPhone(dto.getPhone());
        association.setAddress(dto.getAddress());
        association.setPostalCode(dto.getPostalCode());
        association.setCity(dto.getCity());
        association.setWebsite(dto.getWebsite());
        association.setInvoiceText(dto.getInvoiceText());
        association.setReminderDays(dto.getReminderDays());
        association.setReminderFee(dto.getReminderFee());
        
        return association;
    }
    
    /**
     * Convert a Billing entity to a BillingDTO.
     *
     * @param billing The billing entity
     * @return The converted DTO
     */
    public BillingDTO toBillingDTO(Billing billing) {
        if (billing == null) {
            return null;
        }
        
        BillingDTO dto = new BillingDTO();
        dto.setId(billing.getId());
        dto.setYear(billing.getYear());
        dto.setDescription(billing.getDescription());
        dto.setTotalAmount(billing.getTotalAmount());
        dto.setExtraCharge(billing.getExtraCharge());
        dto.setIssueDate(billing.getIssueDate());
        dto.setDueDate(billing.getDueDate());
        dto.setType(billing.getType() != null ? billing.getType().name() : null);
        
        if (billing.getInvoices() != null) {
            dto.setInvoiceCount(billing.getInvoices().size());
        }
        
        return dto;
    }
    
    /**
     * Convert a list of Billing entities to a list of BillingDTOs.
     *
     * @param billings The list of billing entities
     * @return The list of converted DTOs
     */
    public List<BillingDTO> toBillingDTOList(List<Billing> billings) {
        if (billings == null) {
            return List.of();
        }
        
        return billings.stream()
                .map(this::toBillingDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a BillingDTO to a Billing entity.
     *
     * @param dto The billing DTO
     * @return The converted entity
     */
    public Billing toBillingEntity(BillingDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Billing billing = new Billing();
        billing.setId(dto.getId());
        billing.setYear(dto.getYear());
        billing.setDescription(dto.getDescription());
        billing.setTotalAmount(dto.getTotalAmount());
        billing.setExtraCharge(dto.getExtraCharge());
        billing.setIssueDate(dto.getIssueDate());
        billing.setDueDate(dto.getDueDate());
        
        if (dto.getType() != null) {
            try {
                billing.setType(Billing.BillingType.valueOf(dto.getType()));
            } catch (IllegalArgumentException e) {
                // Use default type if invalid
                billing.setType(Billing.BillingType.ANNUAL_FEE);
            }
        }
        
        return billing;
    }
    
    /**
     * Convert an Owner entity to an OwnerDTO.
     *
     * @param owner The owner entity
     * @return The converted DTO
     */
    public OwnerDTO toOwnerDTO(Owner owner) {
        if (owner == null) {
            return null;
        }
        
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setName(owner.getName());
        dto.setEmail(owner.getEmail());
        dto.setPhone(owner.getPhone());
        dto.setAddress(owner.getAddress());
        dto.setPostalCode(owner.getPostalCode());
        dto.setCity(owner.getCity());
        
        return dto;
    }
    
    /**
     * Convert a list of Owner entities to a list of OwnerDTOs.
     *
     * @param owners The list of owner entities
     * @return The list of converted DTOs
     */
    public List<OwnerDTO> toOwnerDTOList(List<Owner> owners) {
        if (owners == null) {
            return List.of();
        }
        
        return owners.stream()
                .map(this::toOwnerDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert an OwnerDTO to an Owner entity.
     *
     * @param dto The owner DTO
     * @return The converted entity
     */
    public Owner toOwnerEntity(OwnerDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Owner owner = new Owner();
        owner.setId(dto.getId());
        owner.setName(dto.getName());
        owner.setEmail(dto.getEmail());
        owner.setPhone(dto.getPhone());
        owner.setAddress(dto.getAddress());
        owner.setPostalCode(dto.getPostalCode());
        owner.setCity(dto.getCity());
        
        return owner;
    }
    
    /**
     * Convert a Property entity to a PropertyDTO.
     *
     * @param property The property entity
     * @return The converted DTO
     */
    public PropertyDTO toPropertyDTO(Property property) {
        if (property == null) {
            return null;
        }
        
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setPropertyDesignation(property.getPropertyDesignation());
        dto.setShareRatio(property.getShareRatio());
        dto.setAddress(property.getAddress());
        
        // Convert owners
        Set<OwnerDTO> ownerDTOs = new HashSet<>();
        if (property.getOwners() != null) {
            for (Owner owner : property.getOwners()) {
                ownerDTOs.add(toOwnerDTO(owner));
            }
        }
        dto.setOwners(ownerDTOs);
        
        // Convert main contact
        if (property.getMainContact() != null) {
            dto.setMainContact(toOwnerDTO(property.getMainContact()));
        }
        
        return dto;
    }
    
    /**
     * Convert a list of Property entities to a list of PropertyDTOs.
     *
     * @param properties The list of property entities
     * @return The list of converted DTOs
     */
    public List<PropertyDTO> toPropertyDTOList(List<Property> properties) {
        if (properties == null) {
            return List.of();
        }
        
        return properties.stream()
                .map(this::toPropertyDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a PropertyDTO to a Property entity.
     *
     * @param dto The property DTO
     * @return The converted entity
     */
    public Property toPropertyEntity(PropertyDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Property property = new Property();
        property.setId(dto.getId());
        property.setPropertyDesignation(dto.getPropertyDesignation());
        property.setShareRatio(dto.getShareRatio());
        property.setAddress(dto.getAddress());
        
        // Note: We don't set owners and main contact here as they are usually
        // handled separately in service methods
        
        return property;
    }
}
