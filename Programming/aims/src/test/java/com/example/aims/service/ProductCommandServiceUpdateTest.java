package com.example.aims.service;

import com.example.aims.dto.request.UpdateProductRequest;
import com.example.aims.dto.response.ProductResponse;
import com.example.aims.entity.Book;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceUpdateTest {

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

    void stubRepositoriesForSuccess(Book existingBook) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingBook));
        when(userRepository.findById(ACTOR_USER_ID)).thenReturn(Optional.of(actorUser));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updateBook_validRequest_shouldReturnSuccess() {
        Book existingBook = activeBook("Old Title");
        stubRepositoriesForSuccess(existingBook);

        UpdateProductRequest request = validBookUpdateRequest()
                .title("Updated Title")
                .currentPrice(new BigDecimal("60.00"))
                .build();

        ProductResponse response = productCommandService.updateProduct(PRODUCT_ID, request);

        assertEquals(PRODUCT_ID, response.getId());
        assertEquals(ProductType.BOOK, response.getProductType());
        assertEquals("Updated Title", response.getTitle());
        assertEquals(new BigDecimal("60.00"), response.getCurrentPrice());
        assertEquals("Product updated successfully", response.getMessage());

        ArgumentCaptor<Product> savedCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(savedCaptor.capture());
        assertEquals("Updated Title", savedCaptor.getValue().getTitle());

        ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals("UPDATE_PRODUCT", historyCaptor.getValue().getAction());
        assertEquals(actorUser, historyCaptor.getValue().getCreatedByUser());
    }

    @Test
    void updateProduct_notFound_shouldThrow() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        UpdateProductRequest request = validBookUpdateRequest().build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.updateProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), ex.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_alreadyDeleted_shouldThrow() {
        Book deletedBook = activeBook("Deleted Book");
        deletedBook.setStatus("DELETED");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(deletedBook));

        UpdateProductRequest request = validBookUpdateRequest().build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.updateProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_ALREADY_DELETED.getCode(), ex.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_typeChange_shouldThrow() {
        Book existingBook = activeBook("Sample Book");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingBook));

        UpdateProductRequest request = validDvdUpdateRequest().build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.updateProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_TYPE_CHANGE_NOT_ALLOWED.getCode(), ex.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateBook_priceBelow30Percent_shouldThrow() {
        UpdateProductRequest request = validBookUpdateRequest()
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("29.99"))
                .build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.updateProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("30% and 150%"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateBook_userNotFound_shouldThrow() {
        Book existingBook = activeBook("Sample Book");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingBook));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(ACTOR_USER_ID)).thenReturn(Optional.empty());

        UpdateProductRequest request = validBookUpdateRequest().build();

        AppException ex = assertThrows(
                AppException.class, () -> productCommandService.updateProduct(PRODUCT_ID, request));

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    Book activeBook(String title) {
        return Book.builder()
                .id(PRODUCT_ID)
                .title(title)
                .category("Education")
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("50.00"))
                .weight(new BigDecimal("0.50"))
                .length(new BigDecimal("20.00"))
                .height(new BigDecimal("2.00"))
                .width(new BigDecimal("15.00"))
                .stockQuantity(10)
                .status("ACTIVE")
                .publicationDate("2024-01-15")
                .language("English")
                .publisher("HUST Press")
                .coverType("Paperback")
                .nbPages(200)
                .genre("Science")
                .authors(new HashSet<>())
                .build();
    }

    UpdateProductRequest.UpdateProductRequestBuilder validBookUpdateRequest() {
        return UpdateProductRequest.builder()
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
                .updatedByUserId(ACTOR_USER_ID)
                .publicationDate("2024-01-15")
                .language("English")
                .publisher("HUST Press")
                .coverType("Paperback")
                .nbPages(200)
                .genre("Science");
    }

    UpdateProductRequest.UpdateProductRequestBuilder validDvdUpdateRequest() {
        return UpdateProductRequest.builder()
                .productType(ProductType.DVD)
                .title("Sample DVD")
                .category("Movies")
                .originalValue(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("80.00"))
                .weight(new BigDecimal("0.30"))
                .length(new BigDecimal("19.00"))
                .height(new BigDecimal("1.50"))
                .width(new BigDecimal("13.50"))
                .stockQuantity(3)
                .status("ACTIVE")
                .updatedByUserId(ACTOR_USER_ID)
                .releaseDate("2022-05-10")
                .genre("Action")
                .discType("Blu-ray")
                .director("Director X")
                .runtime(120)
                .studio("AIMS Studio")
                .language("English")
                .subtitles("Vietnamese");
    }
}
