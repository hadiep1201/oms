package com.example.aims.service.product.command;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.ProductWriteRequest;
import com.example.aims.dto.request.SectionRequest;
import com.example.aims.entity.Newspaper;
import com.example.aims.entity.Product;
import com.example.aims.entity.Section;
import com.example.aims.enums.ProductType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NewspaperWriteHandler extends AbstractProductWriteHandler {

    ProductWriteSupport support;

    @Override
    public ProductType getProductType() {
        return ProductType.NEWSPAPER;
    }

    @Override
    public boolean supports(Product product) {
        return product instanceof Newspaper;
    }

    @Override
    public void validate(ProductWriteRequest request) {
        support.requireNonBlank(request.getPublicationDate(), "Publication date");
        support.requireNonBlank(request.getLanguage(), "Language");
        support.requireNonBlank(request.getPublisher(), "Publisher");
        support.requireNonBlank(request.getEditorInChief(), "Editor in chief");
        support.requireNonBlank(request.getIssueNumber(), "Issue number");
        support.requireNonBlank(request.getPublicationFrequency(), "Publication frequency");
        support.requireNonBlank(request.getIssn(), "ISSN");
        support.validateDateFormat(request.getPublicationDate(), "Publication date");
    }

    @Override
    public Product build(CreateProductRequest request) {
        Newspaper newspaper = Newspaper.builder()
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
                .editorInChief(request.getEditorInChief())
                .issueNumber(request.getIssueNumber())
                .publicationFrequency(request.getPublicationFrequency())
                .issn(request.getIssn())
                .sections(new HashSet<>())
                .build();

        attachSections(newspaper, request.getSections());
        return newspaper;
    }

    @Override
    public void update(Product product, ProductWriteRequest request) {
        Newspaper newspaper = requireSubtype(product, Newspaper.class);
        applyCommonFields(newspaper, request);
        newspaper.setPublicationDate(request.getPublicationDate());
        newspaper.setLanguage(request.getLanguage());
        newspaper.setPublisher(request.getPublisher());
        newspaper.setEditorInChief(request.getEditorInChief());
        newspaper.setIssueNumber(request.getIssueNumber());
        newspaper.setPublicationFrequency(request.getPublicationFrequency());
        newspaper.setIssn(request.getIssn());
        newspaper.getSections().clear();
        attachSections(newspaper, request.getSections());
    }

    private void attachSections(Newspaper newspaper, List<SectionRequest> sectionRequests) {
        if (sectionRequests == null || sectionRequests.isEmpty()) {
            return;
        }
        Set<Section> sections = new HashSet<>();
        for (SectionRequest sectionRequest : sectionRequests) {
            Section section = Section.builder()
                    .newspaper(newspaper)
                    .title(sectionRequest.getTitle())
                    .description(sectionRequest.getDescription())
                    .build();
            sections.add(section);
        }
        newspaper.setSections(sections);
    }
}
