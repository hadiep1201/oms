package com.example.aims.dto.request;

import com.example.aims.enums.ProductType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Coupling: Stamp coupling with ProductCommandService validate/update helpers (interface exposes all product-type fields)
 * Cohesion: Communicational cohesion
 * Reason: Shared contract for Create and Update requests so common validation/update logic can be reused.
 *
 * SOLID Review:
 * - ISP risk remains at DTO boundary, but type-specific validation/build/update is handled by
 *   ProductTypeWriteHandler implementations so callers no longer depend on all fields at once.
 */
public interface ProductWriteRequest {

    ProductType getProductType();

    String getTitle();

    String getCategory();

    String getGeneralDescription();

    String getBarcode();

    String getImageUrl();

    BigDecimal getOriginalValue();

    BigDecimal getCurrentPrice();

    BigDecimal getWeight();

    BigDecimal getLength();

    BigDecimal getHeight();

    BigDecimal getWidth();

    Integer getStockQuantity();

    String getStatus();

    Integer getActorUserId();

    String getPublicationDate();

    String getLanguage();

    String getPublisher();

    String getCoverType();

    Integer getNbPages();

    String getGenre();

    List<BookAuthorRequest> getAuthors();

    String getEditorInChief();

    String getIssueNumber();

    String getPublicationFrequency();

    String getIssn();

    List<SectionRequest> getSections();

    String getReleaseDate();

    String getArtists();

    String getRecordLabel();

    List<TrackRequest> getTracks();

    String getDiscType();

    String getDirector();

    Integer getRuntime();

    String getStudio();

    String getSubtitles();
}
