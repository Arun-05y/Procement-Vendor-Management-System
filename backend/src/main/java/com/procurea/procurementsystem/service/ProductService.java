package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.ProductDto;
import com.procurea.procurementsystem.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(ProductDto dto);
    ProductDto updateProduct(Long id, ProductDto dto);
    ProductDto getProductById(Long id);
    Page<ProductDto> getAllProducts(String category, Product.ProductStatus status, String search, int page, int size);
    List<ProductDto> getLowStockProducts();
    long countLowStockProducts();
    void deleteProduct(Long id);
    ProductDto updateStock(Long id, int quantityChange);
}
