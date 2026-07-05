package com.example.aims.service.product.command;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.ProductWriteRequest;
import com.example.aims.dto.request.TrackRequest;
import com.example.aims.entity.CD;
import com.example.aims.entity.Product;
import com.example.aims.entity.Track;
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
public class CdWriteHandler extends AbstractProductWriteHandler {

    ProductWriteSupport support;

    @Override
    public ProductType getProductType() {
        return ProductType.CD;
    }

    @Override
    public boolean supports(Product product) {
        return product instanceof CD;
    }

    @Override
    public void validate(ProductWriteRequest request) {
        support.requireNonBlank(request.getReleaseDate(), "Release date");
        support.requireNonBlank(request.getGenre(), "Genre");
        support.requireNonBlank(request.getArtists(), "Artists");
        support.requireNonBlank(request.getRecordLabel(), "Record label");
        support.validateDateFormat(request.getReleaseDate(), "Release date");
    }

    @Override
    public Product build(CreateProductRequest request) {
        CD cd = CD.builder()
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
                .artists(request.getArtists())
                .recordLabel(request.getRecordLabel())
                .tracks(new HashSet<>())
                .build();

        attachTracks(cd, request.getTracks());
        return cd;
    }

    @Override
    public void update(Product product, ProductWriteRequest request) {
        CD cd = requireSubtype(product, CD.class);
        applyCommonFields(cd, request);
        cd.setGenre(request.getGenre());
        cd.setReleaseDate(support.parseSqlDate(request.getReleaseDate()));
        cd.setArtists(request.getArtists());
        cd.setRecordLabel(request.getRecordLabel());
        cd.getTracks().clear();
        attachTracks(cd, request.getTracks());
    }

    private void attachTracks(CD cd, List<TrackRequest> trackRequests) {
        if (trackRequests == null || trackRequests.isEmpty()) {
            return;
        }
        Set<Track> tracks = new HashSet<>();
        for (TrackRequest trackRequest : trackRequests) {
            Track track = Track.builder()
                    .cd(cd)
                    .title(trackRequest.getTitle())
                    .length(trackRequest.getLength())
                    .build();
            tracks.add(track);
        }
        cd.setTracks(tracks);
    }
}
