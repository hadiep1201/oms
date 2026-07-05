package com.example.aims.service.product.command;

import com.example.aims.dto.request.BookAuthorRequest;
import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.ProductWriteRequest;
import com.example.aims.entity.Book;
import com.example.aims.entity.BookAuthor;
import com.example.aims.entity.Product;
import com.example.aims.enums.ProductType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookWriteHandler extends AbstractProductWriteHandler {

    ProductWriteSupport support;

    @Override
    public ProductType getProductType() {
        return ProductType.BOOK;
    }

    @Override
    public boolean supports(Product product) {
        return product instanceof Book;
    }

    @Override
    public void validate(ProductWriteRequest request) {
        support.requireNonBlank(request.getPublicationDate(), "Publication date");
        support.requireNonBlank(request.getLanguage(), "Language");
        support.requireNonBlank(request.getPublisher(), "Publisher");
        support.requireNonBlank(request.getCoverType(), "Cover type");
        support.requireNonNull(request.getNbPages(), "Number of pages");
        support.requireNonBlank(request.getGenre(), "Genre");
        support.validateDateFormat(request.getPublicationDate(), "Publication date");

        if (request.getAuthors() != null) {
            for (BookAuthorRequest author : request.getAuthors()) {
                if (StringUtils.hasText(author.getDob())) {
                    support.validateDateFormat(author.getDob(), "Author date of birth");
                }
            }
        }
    }

    @Override
    public Product build(CreateProductRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .generalDescription(request.getGeneralDescription())
                .barcode(request.getBarcode())
                .imageUrl(request.getImageUrl())
                .originalValue(request.getOriginalValue())
                .currentPrice(request.getCurrentPrice())
                .weight(request.getWeight())
                .length(request.getLength())
                .height(request.getHeight())
                .width(request.getWidth())
                .stockQuantity(request.getStockQuantity())
                .status(request.getStatus())
                .publicationDate(request.getPublicationDate())
                .language(request.getLanguage())
                .publisher(request.getPublisher())
                .coverType(request.getCoverType())
                .nbPages(request.getNbPages())
                .genre(request.getGenre())
                .authors(new HashSet<>())
                .build();

        attachAuthors(book, request.getAuthors());
        return book;
    }

    @Override
    public void update(Product product, ProductWriteRequest request) {
        Book book = requireSubtype(product, Book.class);
        applyCommonFields(book, request);
        book.setPublicationDate(request.getPublicationDate());
        book.setLanguage(request.getLanguage());
        book.setPublisher(request.getPublisher());
        book.setCoverType(request.getCoverType());
        book.setNbPages(request.getNbPages());
        book.setGenre(request.getGenre());
        book.getAuthors().clear();
        attachAuthors(book, request.getAuthors());
    }

    private void attachAuthors(Book book, List<BookAuthorRequest> authorRequests) {
        if (authorRequests == null || authorRequests.isEmpty()) {
            return;
        }
        Set<BookAuthor> authors = new HashSet<>();
        for (BookAuthorRequest authorRequest : authorRequests) {
            BookAuthor author = BookAuthor.builder()
                    .book(book)
                    .name(authorRequest.getName())
                    .dob(StringUtils.hasText(authorRequest.getDob())
                            ? support.parseSqlDate(authorRequest.getDob())
                            : null)
                    .build();
            authors.add(author);
        }
        book.setAuthors(authors);
    }
}
