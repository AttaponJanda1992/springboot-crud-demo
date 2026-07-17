package com.example.crud.controller;

import com.example.crud.exception.ResourceNotFoundException;
import com.example.crud.model.Product;
import com.example.crud.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Product sample() {
        Product p = new Product("Widget", "A useful widget", new BigDecimal("9.99"), 5);
        p.setId(1L);
        return p;
    }

    @Test
    void listReturnsProducts() throws Exception {
        when(service.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Widget"));
    }

    @Test
    void getReturnsProduct() throws Exception {
        when(service.findById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(service.findById(eq(99L))).thenThrow(new ResourceNotFoundException("Product not found: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201() throws Exception {
        Product input = new Product("New", "desc", new BigDecimal("1.00"), 1);
        when(service.create(any(Product.class))).thenReturn(sample());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void createInvalidReturns400() throws Exception {
        Product invalid = new Product("", "desc", new BigDecimal("1.00"), 1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
