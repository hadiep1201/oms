package com.example.aims.subsystemvietqr;

/**
 * Single source of truth for the VietQR payment "content" convention.
 *
 * The QR "content" carries the order reference in the form "AIMS{orderId}".
 * Both QR generation (QRGenerateRequest) and webhook parsing (PayOrderService)
 * go through this class, so the format is defined in exactly one place.
 *
 * Cohesion: Functional - only encodes and decodes the order reference string.
 * Coupling: None - pure static utility, no shared mutable state.
 */
public final class OrderReference {

    private static final String PREFIX = "AIMS";

    private OrderReference() {
    }

    public static String format(Integer orderId) {
        return PREFIX + orderId;
    }

    public static Integer parse(String content) {
        if (content == null || !content.contains(PREFIX)) {
            return null;
        }
        String after = content.substring(content.lastIndexOf(PREFIX) + PREFIX.length()).trim();
        after = after.replace("Order #", "").trim();
        try {
            return Integer.parseInt(after);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}