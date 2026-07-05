package com.example.aims.mapper;

import com.example.aims.dto.response.CDDetailResponse;
import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.CD;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CDMapper implements ProductDetailMapper<CD> {

    @Override
    public boolean supports(Class<?> productClass) {
        return CD.class.isAssignableFrom(productClass);
    }

    @Override
    public ProductDetailResponse mapToResponse(CD cd) {
        var tracks = cd.getTracks().stream()
                .map(t -> t.getTitle() + " (" + t.getLength() + ")")
                .collect(Collectors.toList());

        return CDDetailResponse.builder()
                .id(cd.getId())
                .title(cd.getTitle())
                .category(cd.getCategory())
                .originalValue(cd.getOriginalValue())
                .currentPrice(cd.getCurrentPrice())
                .imageUrl(cd.getImageUrl())
                .stockQuantity(cd.getStockQuantity())
                .generalDescription(cd.getGeneralDescription())
                .status(cd.getStatus())
                .weight(cd.getWeight())
                .length(cd.getLength())
                .height(cd.getHeight())
                .width(cd.getWidth())
                .barcode(cd.getBarcode())
                .artists(cd.getArtists())
                .recordLabels(cd.getRecordLabel())
                .tracksList(tracks)
                .genre(cd.getGenre())
                .releaseDate(cd.getReleaseDate())
                .build();
    }
}
