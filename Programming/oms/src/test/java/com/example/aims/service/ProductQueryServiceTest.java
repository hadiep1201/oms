package com.example.aims.service;

import com.example.aims.dto.response.*;
import com.example.aims.entity.*;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    com.example.aims.mapper.ProductMapperRegistry mapperRegistry;

    @Mock
    com.example.aims.mapper.ProductListMapper productListMapper;

    @InjectMocks
    ProductQueryService productQueryService;

    private Product sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1)
                .title("Clean Code")
                .category("Technology")
                .currentPrice(new BigDecimal("250000"))
                .build();
    }

    @Test
    void getProductDetail_validBookId_returnsBookDetailResponse() {
        BookAuthor author = BookAuthor.builder()
                .name("Robert C. Martin")
                .build();

        Book book = Book.builder()
                .id(1)
                .title("Clean Code")
                .category("Technology")
                .currentPrice(new BigDecimal("250000"))
                .originalValue(new BigDecimal("300000"))
                .stockQuantity(10)
                .status("available")
                .coverType("Hardcover")
                .nbPages(431)
                .genre("Software Engineering")
                .language("English")
                .publisher("Prentice Hall")
                .authors(new HashSet<>(Set.of(author)))
                .build();

        when(productRepository.findById(1)).thenReturn(Optional.of(book));
        when(mapperRegistry.map(any())).thenReturn(BookDetailResponse.builder().title("Clean Code").category("Technology").currentPrice(new BigDecimal("250000")).build());

        ProductDetailResponse result = productQueryService.getProductDetail(1);
        assertInstanceOf(BookDetailResponse.class, result);
        assertEquals("Clean Code", result.getTitle());
        assertEquals("Technology", result.getCategory());
        assertEquals(new BigDecimal("250000"), result.getCurrentPrice());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void getProductDetail_validCdId_returnsCDDetailResponse() {
        Track track = Track.builder()
                .title("Track 1")
                .length(210)
                .build();

        CD cd = CD.builder()
                .id(2)
                .title("The Dark Side of the Moon")
                .category("Music")
                .currentPrice(new BigDecimal("180000"))
                .originalValue(new BigDecimal("200000"))
                .stockQuantity(5)
                .status("available")
                .artists("Pink Floyd")
                .recordLabel("Harvest Records")
                .tracks(new HashSet<>(Set.of(track)))
                .build();

        when(productRepository.findById(2)).thenReturn(Optional.of(cd));
        when(mapperRegistry.map(any())).thenReturn(CDDetailResponse.builder().title("The Dark Side of the Moon").category("Music").currentPrice(new BigDecimal("180000")).build());

        ProductDetailResponse result = productQueryService.getProductDetail(2);
        assertInstanceOf(CDDetailResponse.class, result);
        assertEquals("The Dark Side of the Moon", result.getTitle());
        assertEquals("Music", result.getCategory());
        verify(productRepository, times(1)).findById(2);
    }

    @Test
    void getProductDetail_validDvdId_returnsDVDDetailResponse() {
        DVD dvd = DVD.builder()
                .id(3)
                .title("Inception")
                .category("Movie")
                .currentPrice(new BigDecimal("120000"))
                .originalValue(new BigDecimal("150000"))
                .stockQuantity(8)
                .status("available")
                .director("Christopher Nolan")
                .runtime(148)
                .studio("Warner Bros")
                .language("English")
                .subtitles("Vietnamese")
                .discType("Blu-ray")
                .build();

        when(productRepository.findById(3)).thenReturn(Optional.of(dvd));
        when(mapperRegistry.map(any())).thenReturn(DVDDetailResponse.builder().title("Inception").category("Movie").currentPrice(new BigDecimal("120000")).build());

        ProductDetailResponse result = productQueryService.getProductDetail(3);

        assertInstanceOf(DVDDetailResponse.class, result);
        assertEquals("Inception", result.getTitle());
        assertEquals("Movie", result.getCategory());
        verify(productRepository, times(1)).findById(3);
    }

    @Test
    void getProductDetail_validNewspaperId_returnsNewspaperDetailResponse() {
        Section section = Section.builder()
                .title("Technology")
                .description("Tech news")
                .build();

        Newspaper newspaper = Newspaper.builder()
                .id(4)
                .title("VnExpress")
                .category("Newspaper")
                .currentPrice(new BigDecimal("15000"))
                .originalValue(new BigDecimal("15000"))
                .stockQuantity(50)
                .status("available")
                .editorInChief("Nguyen Van A")
                .publisher("VnExpress Publisher")
                .issueNumber("2025-001")
                .publicationFrequency("Daily")
                .issn("1234-5678")
                .language("Vietnamese")
                .sections(new HashSet<>(Set.of(section)))
                .build();

        when(productRepository.findById(4)).thenReturn(Optional.of(newspaper));
        when(mapperRegistry.map(any())).thenReturn(NewspaperDetailResponse.builder().title("VnExpress").category("Newspaper").currentPrice(new BigDecimal("15000")).build());

        ProductDetailResponse result = productQueryService.getProductDetail(4);

        assertInstanceOf(NewspaperDetailResponse.class, result);
        assertEquals("VnExpress", result.getTitle());
        assertEquals("Newspaper", result.getCategory());
        verify(productRepository, times(1)).findById(4);
    }

    @Test
    void getProductDetail_repositoryThrowsRuntimeException_exceptionPropagates() {
        when(productRepository.findById(1))
                .thenThrow(new RuntimeException("DB connection failed"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> productQueryService.getProductDetail(1)
        );
        assertEquals("DB connection failed", ex.getMessage());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void getProductDetail_idNotFound_throwsResourceNotFoundException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productQueryService.getProductDetail(999)
        );
        assertTrue(ex.getMessage().contains("Cannot find product with id: 999"));
        verify(productRepository, times(1)).findById(999);
    }

    @Test
    void getProductDetail_nullId_throwsIllegalArgumentException() {
        when(productRepository.findById(null))
                .thenThrow(new IllegalArgumentException("The given id must not be null!"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> productQueryService.getProductDetail(null)
        );
        assertTrue(ex.getMessage().contains("null"));
        verify(productRepository, times(1)).findById(null);
    }

    @Test
    void getFeaturedProducts_mapsFirstTwelveProductsForHomepage() {
        Product product = Book.builder()
                .id(1)
                .title("Clean Code")
                .category("Book")
                .imageUrl("https://example.com/clean-code.png")
                .originalValue(new BigDecimal("300000"))
                .currentPrice(new BigDecimal("250000"))
                .build();

        org.mockito.Mockito.lenient().when(productRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(product)));
        org.mockito.Mockito.lenient().when(productRepository.findAll()).thenReturn(java.util.List.of(product));
        when(productListMapper.toHomepageResponse(any())).thenReturn(
                ProductHomepageResponse.builder()
                        .id(1)
                        .title("Clean Code")
                        .category("Book")
                        .imageUrl("https://example.com/clean-code.png")
                        .originalValue(new BigDecimal("300000"))
                        .currentPrice(new BigDecimal("250000"))
                        .build()
        );

        List<ProductHomepageResponse> result = productQueryService.getFeaturedProducts();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Clean Code", result.get(0).getTitle());
        assertEquals("Book", result.get(0).getCategory());
        assertEquals("https://example.com/clean-code.png", result.get(0).getImageUrl());
        assertEquals(new BigDecimal("300000"), result.get(0).getOriginalValue());
        assertEquals(new BigDecimal("250000"), result.get(0).getCurrentPrice());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void searchProducts_validKeywordNoPriceFilter_returnsMatchingList() {
        when(productRepository.searchProducts("Clean", null, null))
                .thenReturn(List.of(sampleBook));
        when(productListMapper.toSearchResponse(any())).thenReturn(
                ProductSearchResponse.builder().title("Clean Code").build()
        );

        List<ProductSearchResponse> result = productQueryService.searchProducts("Clean", null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
        verify(productRepository, times(1)).searchProducts("Clean", null, null);
    }

    @Test
    void searchProducts_validKeywordWithPriceRange_returnsFilteredList() {
        BigDecimal minPrice = new BigDecimal("100000");
        BigDecimal maxPrice = new BigDecimal("300000");

        when(productRepository.searchProducts("Clean", minPrice, maxPrice))
                .thenReturn(List.of(sampleBook));
        when(productListMapper.toSearchResponse(any())).thenReturn(
                ProductSearchResponse.builder().title("Clean Code").build()
        );

        List<ProductSearchResponse> result = productQueryService.searchProducts("Clean", minPrice, maxPrice);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
        verify(productRepository, times(1)).searchProducts("Clean", minPrice, maxPrice);
    }

    @Test
    void searchProducts_validKeywordNoResultsFound_throwsResourceNotFoundException() {
        when(productRepository.searchProducts("xyzxyz", null, null))
                .thenReturn(Collections.emptyList());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productQueryService.searchProducts("xyzxyz", null, null)
        );
        assertTrue(ex.getMessage().contains("xyzxyz"));
        verify(productRepository, times(1)).searchProducts("xyzxyz", null, null);
    }
}
