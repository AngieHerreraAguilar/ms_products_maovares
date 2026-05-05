package com.maovares.ms_products.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.maovares.ms_products.product.application.port.in.CreateProductCommand;
import com.maovares.ms_products.product.application.port.out.ProductRepository;
import com.maovares.ms_products.product.domain.model.Product;
import com.maovares.ms_products.product.infraestructure.queue.QueueService;

@Service
public class CreateProductService implements CreateProductCommand {

    private final ProductRepository productRepository;
    private final QueueService queueService;

    public CreateProductService(ProductRepository productRepository, QueueService queueService) {
        this.productRepository = productRepository;
        this.queueService = queueService;
    }

    @Override
    public Product execute(String description, double price, String image, String title) {
        String id = UUID.randomUUID().toString();

        Product product = new Product(id, price, description, image, title);
        Product saved = productRepository.save(product);

        String message = String.format(
            "{\"orderId\":\"%s\",\"customerEmail\":\"herreraangie.0506@gmail.com\",\"customerName\":\"Angie Herrera\",\"total\":%.2f,\"items\":[{\"sku\":\"%s\",\"qty\":1}]}",
            saved.getId(), saved.getPrice(), saved.getTitle()
        );
        queueService.sendMessage(message);

        return saved;
    }
}