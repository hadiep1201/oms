package com.example.aims.service.product.command;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.ProductWriteRequest;
import com.example.aims.entity.DVD;
import com.example.aims.entity.Product;
import com.example.aims.enums.ProductType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DvdWriteHandler extends AbstractProductWriteHandler {

    ProductWriteSupport support;

    @Override
    public ProductType getProductType() {
        return ProductType.DVD;
    }

    @Override
    public boolean supports(Product product) {
        return product instanceof DVD;
    }

    @Override
    public void validate(ProductWriteRequest request) {
        support.requireNonBlank(request.getReleaseDate(), "Release date");
        support.requireNonBlank(request.getGenre(), "Genre");
        support.requireNonBlank(request.getDiscType(), "Disc type");
        support.requireNonBlank(request.getDirector(), "Director");
        support.requireNonNull(request.getRuntime(), "Runtime");
        support.requireNonBlank(request.getStudio(), "Studio");
        support.requireNonBlank(request.getLanguage(), "Language");
        support.requireNonBlank(request.getSubtitles(), "Subtitles");
        support.validateDateFormat(request.getReleaseDate(), "Release date");
    }

    @Override
    public Product build(CreateProductRequest request) {
        return DVD.builder()
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
                .genre(request.getGenre())
                .releaseDate(support.parseSqlDate(request.getReleaseDate()))
                .discType(request.getDiscType())
                .director(request.getDirector())
                .runtime(request.getRuntime())
                .studio(request.getStudio())
                .language(request.getLanguage())
                .subtitles(request.getSubtitles())
                .build();
    }

    @Override
    public void update(Product product, ProductWriteRequest request) {
        DVD dvd = requireSubtype(product, DVD.class);
        applyCommonFields(dvd, request);
        dvd.setGenre(request.getGenre());
        dvd.setReleaseDate(support.parseSqlDate(request.getReleaseDate()));
        dvd.setDiscType(request.getDiscType());
        dvd.setDirector(request.getDirector());
        dvd.setRuntime(request.getRuntime());
        dvd.setStudio(request.getStudio());
        dvd.setLanguage(request.getLanguage());
        dvd.setSubtitles(request.getSubtitles());
    }
}
