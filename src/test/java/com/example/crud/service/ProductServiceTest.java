package com.example.crud.service;

import com.example.crud.exception.ResourceNotFoundException;
import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product sample() {
        Product p = new Product("Widget", "A useful widget", new BigDecimal("9.99"), 5);
        p.setId(1L);
        return p;
    }

    @Test
    void findAllReturnsProducts() {
        when(repository.findAll()).thenReturn(List.of(sample()));

        List<Product> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Widget");
    }

    @Test
    void findByIdReturnsProductWhenPresent() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample()));

        Product result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createResetsIdAndSaves() {
        Product input = new Product("New", "desc", new BigDecimal("1.00"), 1);
        input.setId(123L);
        when(repository.save(any(Product.class))).thenReturn(sample());

        Product result = service.create(input);

        assertThat(input.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Widget");
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    void updateModifiesExistingProduct() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample()));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = new Product("Renamed", "new desc", new BigDecimal("2.50"), 10);
        Product result = service.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Renamed");
        assertThat(result.getQuantity()).isEqualTo(10);
    }

    @Test
    void deleteRemovesExistingProduct() {
        Product existing = sample();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository, times(1)).delete(existing);
    }
}
