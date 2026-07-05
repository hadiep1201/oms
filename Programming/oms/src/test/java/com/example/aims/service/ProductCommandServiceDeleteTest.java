package com.example.aims.service;

import com.example.aims.dto.request.DeleteProductRequest;
import com.example.aims.dto.response.DeleteProductResponse;
import com.example.aims.entity.History;
import com.example.aims.entity.Product;
import com.example.aims.entity.User;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.repository.HistoryRepository;
import com.example.aims.repository.OrderDetailRepository;
import com.example.aims.repository.ProductRepository;
import com.example.aims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceDeleteTest {

    static final int PRODUCT_ID = 1;
    static final int ACTOR_USER_ID = 1;

    @Mock
    ProductRepository productRepository;

    @Mock
    HistoryRepository historyRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    OrderDetailRepository orderDetailRepository;

    ProductCommandService productCommandService;

    User actorUser;

    @BeforeEach
    void setUp() {
        actorUser = User.builder()
                .userId(ACTOR_USER_ID)
                .userName("manager")
                .email("manager@test.com")
                .hashedPassword("secret")
                .build();

        productCommandService = ProductCommandServiceTestFactory.create(
                productRepository,
                orderDetailRepository,
                historyRepository,
                userRepository
        );
    }

    void stubRepositoriesForSuccess(Product product) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderDetailRepository.existsByProduct_Id(PRODUCT_ID)).thenReturn(false);
        when(userRepository.findById(ACTOR_USER_ID)).thenReturn(Optional.of(actorUser));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deleteProduct_validRequest_shouldReturnSuccess() {
        Product product = activeProduct();
        stubRepositoriesForSuccess(product);

        DeleteProductRequest request = DeleteProductRequest.builder()
                .deletedByUserId(ACTOR_USER_ID)
                .build();

        DeleteProductResponse response = productCommandService.deleteProduct(PRODUCT_ID, request);

        assertEquals(PRODUCT_ID, response.getId());
        assertEquals("DEACTIVATED", response.getStatus());
        assertEquals("Product deactivated successfully", response.getMessage());

        ArgumentCaptor<Product> savedCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(savedCaptor.capture());
        assertEquals("DEACTIVATED", savedCaptor.getValue().getStatus());

        ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals("DELETE_PRODUCT", historyCaptor.getValue().getAction());
        assertEquals(actorUser, historyCaptor.getValue().getCreatedByUser());
    }

    @Test
    void deleteProduct_zeroStock_shouldMarkDeleted() {
        Product product = activeProduct();
        product.setStockQuantity(0);
        stubRepositoriesForSuccess(product);

        DeleteProductRequest request = DeleteProductRequest.builder()
                .deletedByUserId(ACTOR_USER_ID)
                .build();

        DeleteProductResponse response = productCommandService.deleteProduct(PRODUCT_ID, request);

        assertEquals("DELETED", response.getStatus());
        assertEquals("Product deleted successfully", response.getMessage());

        ArgumentCaptor<Product> savedCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(savedCaptor.capture());
        assertEquals("DELETED", savedCaptor.getValue().getStatus());
    }

    @Test
    void deleteProduct_notFound_shouldThrow() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        DeleteProductRequest request = DeleteProductRequest.builder()
                .deletedByUserId(ACTOR_USER_ID)
                .build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.deleteProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), ex.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_alreadyDeleted_shouldThrow() {
        Product deletedProduct = activeProduct();
        deletedProduct.setStatus("DELETED");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(deletedProduct));

        DeleteProductRequest request = DeleteProductRequest.builder()
                .deletedByUserId(ACTOR_USER_ID)
                .build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.deleteProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_ALREADY_DELETED.getCode(), ex.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_inOrder_shouldThrow() {
        Product product = activeProduct();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderDetailRepository.existsByProduct_Id(PRODUCT_ID)).thenReturn(true);

        DeleteProductRequest request = DeleteProductRequest.builder()
                .deletedByUserId(ACTOR_USER_ID)
                .build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.deleteProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_IN_ORDER.getCode(), ex.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_userNotFound_shouldThrow() {
        Product product = activeProduct();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderDetailRepository.existsByProduct_Id(PRODUCT_ID)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(ACTOR_USER_ID)).thenReturn(Optional.empty());

        DeleteProductRequest request = DeleteProductRequest.builder()
                .deletedByUserId(ACTOR_USER_ID)
                .build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.deleteProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    Product activeProduct() {
        return com.example.aims.entity.Book.builder()
                .id(PRODUCT_ID)
                .title("Sample Product")
                .category("General")
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("50.00"))
                .weight(new BigDecimal("1.00"))
                .length(new BigDecimal("10.00"))
                .height(new BigDecimal("2.00"))
                .width(new BigDecimal("8.00"))
                .stockQuantity(5)
                .status("ACTIVE")
                .build();
    }
}
