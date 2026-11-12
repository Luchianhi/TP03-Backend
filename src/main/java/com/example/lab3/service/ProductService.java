package com.example.lab3.service;

import com.example.lab3.dto.ProductDto;
import com.example.lab3.entity.Category;
import com.example.lab3.entity.Product;
import com.example.lab3.repository.CategoryRepository;
import com.example.lab3.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    @Transactional
    public ProductDto create(ProductDto dto) {
        Category cat = categoryRepo.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        Product p = new Product(null, dto.name(), dto.price(), cat);
        Product saved = productRepo.save(p);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), cat.getId());
    }

    // Méthodes read, update, delete similaires

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return toDto(p);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Category cat = categoryRepo.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        p.setName(dto.name());
        p.setPrice(dto.price());
        p.setCategory(cat);
        return toDto(productRepo.save(p));
    }

    @Transactional
    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {   // pour l'étape 13
        return productRepo.findAll(pageable).map(this::toDto);
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(p.getId(), p.getName(), p.getPrice(),
                p.getCategory() != null ? p.getCategory().getId() : null);
    }
}