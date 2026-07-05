package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookDetailResponse extends ProductDetailResponse {
    String authors;
    String coverType;
    String publisher;
    String publicationDate;
    Integer pages;
    String language;
    String genre;
}
