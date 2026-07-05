package com.example.aims.mapper;

import com.example.aims.dto.response.NewspaperDetailResponse;
import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.Newspaper;
import com.example.aims.entity.Section;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class NewspaperMapper implements ProductDetailMapper<Newspaper> {

    @Override
    public boolean supports(Class<?> productClass) {
        return Newspaper.class.isAssignableFrom(productClass);
    }

    @Override
    public ProductDetailResponse mapToResponse(Newspaper newspaper) {
        String sections = newspaper.getSections().stream()
                .map(Section::getTitle)
                .collect(Collectors.joining(", "));

        return NewspaperDetailResponse.builder()
                .id(newspaper.getId())
                .title(newspaper.getTitle())
                .category(newspaper.getCategory())
                .originalValue(newspaper.getOriginalValue())
                .currentPrice(newspaper.getCurrentPrice())
                .imageUrl(newspaper.getImageUrl())
                .stockQuantity(newspaper.getStockQuantity())
                .generalDescription(newspaper.getGeneralDescription())
                .status(newspaper.getStatus())
                .weight(newspaper.getWeight())
                .length(newspaper.getLength())
                .height(newspaper.getHeight())
                .width(newspaper.getWidth())
                .barcode(newspaper.getBarcode())
                .editorInChief(newspaper.getEditorInChief())
                .publisher(newspaper.getPublisher())
                .publicationDate(newspaper.getPublicationDate())
                .issueNumber(newspaper.getIssueNumber())
                .publicationFrequency(newspaper.getPublicationFrequency())
                .issn(newspaper.getIssn())
                .language(newspaper.getLanguage())
                .sections(sections)
                .build();
    }
}
