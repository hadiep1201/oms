package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CDDetailResponse extends ProductDetailResponse {
    String artists;
    String recordLabels;
    List<String> tracksList;
    String genre;
    Date releaseDate;
}
