package com.billerby.roadvault.repository;

import com.billerby.roadvault.config.TestH2Config;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.model.Property;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PropertyRepository using H2 in-memory database.
 */
@DataJpaTest
@Import(TestH2Config.class)
@ActiveProfiles("test")
public class PropertyRepositoryIntegrationTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    public void testSaveAndFindProperty() {
        // Create and save owner
        Owner owner = new Owner();
        owner.setName("Test Owner");
        owner.setEmail("test@example.com");
        owner.setAddress("Test Address");
        Owner savedOwner = ownerRepository.save(owner);
        
        // Create property with main contact and save
        Property property = new Property();
        property.setPropertyDesignation("Mjörn Apelgården 1:99");
        property.setShareRatio(new BigDecimal("3.640"));
        property.setAddress("Test Property Address");
        property.addOwner(savedOwner);
        property.setMainContact(savedOwner);
        
        Property savedProperty = propertyRepository.save(property);
        
        // Verify property was saved correctly
        assertNotNull(savedProperty.getId());
        assertEquals("Mjörn Apelgården 1:99", savedProperty.getPropertyDesignation());
        assertEquals(new BigDecimal("3.640"), savedProperty.getShareRatio());
        assertEquals("Test Property Address", savedProperty.getAddress());
        
        // Verify owner relationship
        assertTrue(savedProperty.getOwners().contains(savedOwner));
        assertEquals(savedOwner, savedProperty.getMainContact());
        
        // Test finding by ID
        Optional<Property> foundProperty = propertyRepository.findById(savedProperty.getId());
        assertTrue(foundProperty.isPresent());
        assertEquals("Mjörn Apelgården 1:99", foundProperty.get().getPropertyDesignation());
        
        // Test finding with owner
        Optional<Property> foundPropertyWithOwner = propertyRepository.findByIdWithOwner(savedProperty.getId());
        assertTrue(foundPropertyWithOwner.isPresent());
        assertEquals(1, foundPropertyWithOwner.get().getOwners().size());
        assertEquals("Test Owner", foundPropertyWithOwner.get().getMainContact().getName());
        
        // Test finding by designation
        List<Property> propertiesByDesignation = propertyRepository.findByPropertyDesignationContainingIgnoreCase("Apelgården");
        assertFalse(propertiesByDesignation.isEmpty());
        assertEquals(1, propertiesByDesignation.size());
        assertEquals("Mjörn Apelgården 1:99", propertiesByDesignation.get(0).getPropertyDesignation());
    }
}
