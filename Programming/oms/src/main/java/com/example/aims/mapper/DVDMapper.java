package com.example.aims.mapper;

import com.example.aims.dto.response.DVDDetailResponse;
import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.DVD;
import org.springframework.stereotype.Component;

@Component
public class DVDMapper implements ProductDetailMapper<DVD> {

    @Override
    public boolean supports(Class<?> productClass) {
        return DVD.class.isAssignableFrom(productClass);
    }

    @Override
    public ProductDetailResponse mapToResponse(DVD dvd) {
        return DVDDetailResponse.builder()
                .id(dvd.getId())
                .title(dvd.getTitle())
                .category(dvd.getCategory())
                .originalValue(dvd.getOriginalValue())
                .currentPrice(dvd.getCurrentPrice())
                .imageUrl(dvd.getImageUrl())
                .stockQuantity(dvd.getStockQuantity())
                .generalDescription(dvd.getGeneralDescription())
                .status(dvd.getStatus())
                .weight(dvd.getWeight())
                .length(dvd.getLength())
                .height(dvd.getHeight())
                .width(dvd.getWidth())
                .barcode(dvd.getBarcode())
                .discType(dvd.getDiscType())
                .director(dvd.getDirector())
                .runtime(dvd.getRuntime())
                .studio(dvd.getStudio())
                .language(dvd.getLanguage())
                .subtitles(dvd.getSubtitles())
                .releaseDate(dvd.getReleaseDate())
                .genre(dvd.getGenre())
                .build();
    }
}
