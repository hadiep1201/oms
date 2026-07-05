package com.example.aims.service;

import com.example.aims.service.product.command.*;
import com.example.aims.repository.HistoryRepository;
import com.example.aims.repository.OrderDetailRepository;
import com.example.aims.repository.ProductRepository;
import com.example.aims.repository.UserRepository;

import java.util.List;

final class ProductCommandServiceTestFactory {

    private ProductCommandServiceTestFactory() {
    }

    static ProductCommandService create(
            ProductRepository productRepository,
            OrderDetailRepository orderDetailRepository,
            HistoryRepository historyRepository,
            UserRepository userRepository) {
        ProductWriteSupport support = new ProductWriteSupport();
        ProductPriceValidator priceValidator = new ProductPriceValidator();
        ProductTypeWriteHandlerRegistry registry = new ProductTypeWriteHandlerRegistry(List.of(
                new BookWriteHandler(support),
                new NewspaperWriteHandler(support),
                new CdWriteHandler(support),
                new DvdWriteHandler(support)
        ));
        ProductHistoryRecorder historyRecorder = new ProductHistoryRecorder(userRepository, historyRepository);
        ProductCommandResponseMapper responseMapper = new ProductCommandResponseMapper();

        return new ProductCommandService(
                productRepository,
                orderDetailRepository,
                priceValidator,
                registry,
                historyRecorder,
                responseMapper
        );
    }
}
