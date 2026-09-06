package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.ProductDto;
import com.procurea.procurementsystem.entity.Product;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.ProductRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.setProductCode(dto.getProductCode() == null || dto.getProductCode().isBlank() 
                ? "PRD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() 
                : dto.getProductCode());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setDescription(dto.getDescription());
        product.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : 0.0);
        product.setCurrentStock(dto.getCurrentStock() != null ? dto.getCurrentStock() : 0);
        product.setMinimumStockLevel(dto.getMinimumStockLevel() != null ? dto.getMinimumStockLevel() : 10);
        product.setUnit(dto.getUnit() != null ? dto.getUnit() : "Units");

        if (dto.getSupplierId() != null) {
            Vendor supplier = vendorRepository.findById(dto.getSupplierId()).orElse(null);
            product.setSupplier(supplier);
        }

        product.updateStockStatus();
        Product saved = productRepository.save(product);

        auditLogService.log("SYSTEM", "CREATE_PRODUCT", "Product", saved.getId(), null, saved.getName());

        if (saved.isLowStock()) {
            notifyLowStock(saved);
        }

        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        String oldVal = product.getName() + " (Stock: " + product.getCurrentStock() + ")";

        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setDescription(dto.getDescription());
        product.setUnitPrice(dto.getUnitPrice());
        product.setCurrentStock(dto.getCurrentStock());
        product.setMinimumStockLevel(dto.getMinimumStockLevel());
        product.setUnit(dto.getUnit());

        if (dto.getSupplierId() != null) {
            Vendor supplier = vendorRepository.findById(dto.getSupplierId()).orElse(null);
            product.setSupplier(supplier);
        }

        product.updateStockStatus();
        Product updated = productRepository.save(product);

        auditLogService.log("SYSTEM", "UPDATE_PRODUCT", "Product", updated.getId(), oldVal, updated.getName() + " (Stock: " + updated.getCurrentStock() + ")");

        if (updated.isLowStock()) {
            notifyLowStock(updated);
        }

        return convertToDto(updated);
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDto(product);
    }

    @Override
    public Page<ProductDto> getAllProducts(String category, Product.ProductStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> products = productRepository.searchProducts(
                category == null || category.isBlank() ? null : category,
                status,
                search == null || search.isBlank() ? null : search,
                pageable
        );
        return products.map(this::convertToDto);
    }

    @Override
    public List<ProductDto> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countLowStockProducts() {
        return productRepository.countLowStockProducts();
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
        auditLogService.log("SYSTEM", "DELETE_PRODUCT", "Product", id, product.getName(), "DELETED");
    }

    @Override
    @Transactional
    public ProductDto updateStock(Long id, int quantityChange) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int newStock = Math.max(0, (product.getCurrentStock() != null ? product.getCurrentStock() : 0) + quantityChange);
        product.setCurrentStock(newStock);
        product.updateStockStatus();
        Product saved = productRepository.save(product);

        if (saved.isLowStock()) {
            notifyLowStock(saved);
        }

        return convertToDto(saved);
    }

    private void notifyLowStock(Product product) {
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> 
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_PROCUREMENT_MANAGER ||
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_PROCUREMENT_EXECUTIVE ||
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_ADMIN))
                .collect(Collectors.toList());

        for (User manager : managers) {
            notificationService.sendNotification(
                    manager.getId(),
                    "Low Stock Alert: Product '" + product.getName() + "' has only " + product.getCurrentStock() + " " + product.getUnit() + " remaining (Min: " + product.getMinimumStockLevel() + ").",
                    "LOW_STOCK"
            );
        }
    }

    private ProductDto convertToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setProductCode(p.getProductCode());
        dto.setName(p.getName());
        dto.setCategory(p.getCategory());
        dto.setDescription(p.getDescription());
        dto.setUnitPrice(p.getUnitPrice());
        dto.setCurrentStock(p.getCurrentStock());
        dto.setMinimumStockLevel(p.getMinimumStockLevel());
        dto.setUnit(p.getUnit());
        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierName(p.getSupplier().getCompanyName());
        }
        dto.setStatus(p.getStatus() != null ? p.getStatus().name() : "ACTIVE");
        dto.setLowStock(p.isLowStock());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
