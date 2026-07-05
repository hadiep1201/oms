package com.example.aims.service;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.response.ProductResponse;
import com.example.aims.entity.History;
import com.example.aims.entity.Product;
import com.example.aims.entity.User;
import com.example.aims.enums.ProductType;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.repository.HistoryRepository;
import com.example.aims.repository.OrderDetailRepository;
import com.example.aims.repository.ProductRepository;
import com.example.aims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceCreateTest {

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

    void stubRepositoriesForSuccess() {
        when(userRepository.findById(ACTOR_USER_ID)).thenReturn(Optional.of(actorUser));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(1);
            return product;
        });
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createBook_validRequest_shouldReturnSuccess() {
        stubRepositoriesForSuccess();
        CreateProductRequest request = validBookRequest()
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("50.00"))
                .build();

        ProductResponse response = productCommandService.createProduct(request);

        assertEquals(1, response.getId());
        assertEquals(ProductType.BOOK, response.getProductType());
        assertEquals("Sample Book", response.getTitle());
        assertEquals("Product created successfully", response.getMessage());

        verify(productRepository).save(any(Product.class));

        ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals("ADD_PRODUCT", historyCaptor.getValue().getAction());
        assertEquals(actorUser, historyCaptor.getValue().getCreatedByUser());
    }

    @Test
    void createCd_validRequest_shouldReturnSuccess() {
        stubRepositoriesForSuccess();
        CreateProductRequest request = validCdRequest()
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("80.00"))
                .build();

        ProductResponse response = productCommandService.createProduct(request);

        assertEquals(1, response.getId());
        assertEquals(ProductType.CD, response.getProductType());
        assertEquals("Sample Album", response.getTitle());
        assertEquals("Product created successfully", response.getMessage());

        verify(productRepository).save(any(Product.class));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void createBook_priceBelow30Percent_shouldThrow() {
        CreateProductRequest request = validBookRequest()
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("29.99"))
                .build();

        AppException ex = assertThrows(AppException.class, () -> productCommandService.createProduct(request));

        assertEquals(ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("30% and 150%"));
    }

    @Test
    void createBook_priceAbove150Percent_shouldThrow() {
        CreateProductRequest request = validBookRequest()
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("150.01"))
                .build();

        AppException ex = assertThrows(AppException.class, () -> productCommandService.createProduct(request));

        assertEquals(ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("30% and 150%"));
    }

    @DisplayName("createBook - Current price exactly at boundary values should succeed")
    @Test
    void createBook_priceAtBoundaries_shouldSucceed() {
        // This is replaced by ParameterizedTest below, but kept for compilation reference if needed
    }

    @ParameterizedTest
    @CsvSource({"30.00", "150.00"})
    void createBook_priceAtBoundaries_shouldSucceedParameterized(String currentPrice) {
        stubRepositoriesForSuccess();
        CreateProductRequest request = validBookRequest()
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal(currentPrice))
                .build();

        ProductResponse response = productCommandService.createProduct(request);

        assertEquals("Product created successfully", response.getMessage());
        assertEquals(new BigDecimal(currentPrice), response.getCurrentPrice());
    }

    @Test
    void createBook_missingPublisher_shouldThrow() {
        CreateProductRequest request = validBookRequest()
                .publisher("")
                .build();

        AppException ex = assertThrows(AppException.class, () -> productCommandService.createProduct(request));

        assertEquals(ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("Publisher is required"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createBook_invalidPublicationDate_shouldThrow() {
        CreateProductRequest request = validBookRequest()
                .publicationDate("31/12/2024")
                .build();

        AppException ex = assertThrows(AppException.class, () -> productCommandService.createProduct(request));

        assertEquals(ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("Invalid date format"));
        assertTrue(ex.getMessage().contains("yyyy-MM-dd"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createBook_userNotFound_shouldThrow() {
        when(userRepository.findById(ACTOR_USER_ID)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(1);
            return product;
        });

        CreateProductRequest request = validBookRequest().build();

        AppException ex = assertThrows(AppException.class, () -> productCommandService.createProduct(request));

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), ex.getCode());
        verify(productRepository).save(any(Product.class));
        verify(historyRepository, never()).save(any());
    }

    private CreateProductRequest.CreateProductRequestBuilder validBookRequest() {
        return CreateProductRequest.builder()
                .productType(ProductType.BOOK)
                .title("Sample Book")
                .category("Education")
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("50.00"))
                .weight(new BigDecimal("0.50"))
                .length(new BigDecimal("20.00"))
                .height(new BigDecimal("2.00"))
                .width(new BigDecimal("15.00"))
                .stockQuantity(10)
                .status("ACTIVE")
                .createdByUserId(ACTOR_USER_ID)
                .publicationDate("2024-01-15")
                .language("English")
                .publisher("HUST Press")
                .coverType("Paperback")
                .nbPages(200)
                .genre("Science");
    }

    private CreateProductRequest.CreateProductRequestBuilder validCdRequest() {
        return CreateProductRequest.builder()
                .productType(ProductType.CD)
                .title("Sample Album")
                .category("Music")
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("80.00"))
                .weight(new BigDecimal("0.20"))
                .length(new BigDecimal("14.00"))
                .height(new BigDecimal("1.00"))
                .width(new BigDecimal("12.50"))
                .stockQuantity(5)
                .status("ACTIVE")
                .createdByUserId(ACTOR_USER_ID)
                .releaseDate("2023-06-01")
                .genre("Pop")
                .artists("Artist A")
                .recordLabel("AIMS Records");
    }
}
