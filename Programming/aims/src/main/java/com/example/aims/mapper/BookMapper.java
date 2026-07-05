package com.example.aims.mapper;

import com.example.aims.dto.response.BookDetailResponse;
import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.Book;
import com.example.aims.entity.BookAuthor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BookMapper implements ProductDetailMapper<Book> {

    @Override
    public boolean supports(Class<?> productClass) {
        return Book.class.isAssignableFrom(productClass);
    }

    @Override
    public ProductDetailResponse mapToResponse(Book book) {
        String authors = book.getAuthors().stream()
                .map(BookAuthor::getName)
                .collect(Collectors.joining(", "));

        return BookDetailResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .category(book.getCategory())
                .originalValue(book.getOriginalValue())
                .currentPrice(book.getCurrentPrice())
                .imageUrl(book.getImageUrl())
                .stockQuantity(book.getStockQuantity())
                .generalDescription(book.getGeneralDescription())
                .status(book.getStatus())
                .weight(book.getWeight())
                .length(book.getLength())
                .height(book.getHeight())
                .width(book.getWidth())
                .barcode(book.getBarcode())
                .authors(authors)
                .coverType(book.getCoverType())
                .publisher(book.getPublisher())
                .publicationDate(book.getPublicationDate())
                .pages(book.getNbPages())
                .language(book.getLanguage())
                .genre(book.getGenre())
                .build();
    }
}
