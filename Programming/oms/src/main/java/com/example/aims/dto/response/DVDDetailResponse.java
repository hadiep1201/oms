package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVDDetailResponse extends ProductDetailResponse {
    String discType;
    String director;
    Integer runtime;
    String studio;
    String language;
    String subtitles;
    Date releaseDate;
    String genre;
}
