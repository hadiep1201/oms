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
public class NewspaperDetailResponse extends ProductDetailResponse {
    String editorInChief;
    String publisher;
    String publicationDate;
    String issueNumber;
    String publicationFrequency;
    String issn;
    String language;
    String sections;
}
