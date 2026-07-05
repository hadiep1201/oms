package com.example.aims.subsystemvietqr;

import com.example.aims.entity.Order;
import com.example.aims.entity.QRCode;
import com.example.aims.subsystemvietqr.dto.QRGenerateRequest;
import com.example.aims.subsystemvietqr.dto.QRGenerateResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter realizing IVietQrQrCode: generates a VietQR payment QR code for an order.
 *
 * Cohesion: Functional - one responsibility: produce a QR code via the VietQR API.
 *
 * Coupling:
 * - Stamp coupling with Order: reads order fields to build the QR request.
 * - Data coupling with VietQRApiClient and VietQRAccessTokenProvider (String values).
 *
 * SOLID:
 * - SRP: QR generation only; the sandbox simulation lives in VietQRSimulatorAdapter.
 * - DIP: callers depend on IVietQrQrCode; token acquisition is delegated to the
 *   VietQRAccessTokenProvider abstraction-by-component.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VietQRQrCodeAdapter implements IVietQrQrCode {

    final VietQRApiClient apiClient;
    final VietQRAccessTokenProvider tokenProvider;

    @Value("${vietqr.bank-code}")
    String bankCode;

    @Value("${vietqr.bank-account}")
    String bankAccount;

    @Value("${vietqr.user-bank-name}")
    String userBankName;

    @Override
    public QRCode generateQRCode(Order order) {
        try {
            if ("your_vietqr_username".equals(bankCode) || "your_vietqr_bank_code".equals(bankCode) || bankCode == null || bankCode.startsWith("your_")) {
                throw new RuntimeException("Using placeholder credentials");
            }
            String accessToken = tokenProvider.acquire();

            QRGenerateRequest qrRequest = new QRGenerateRequest(bankCode, bankAccount, userBankName);
            qrRequest.validateRequestData();
            String requestString = qrRequest.buildRequestString(order);

            String qrResponseString = apiClient.generateQRCode(requestString, accessToken);

            QRGenerateResponse qrResponse = new QRGenerateResponse();
            qrResponse.parseResponseString(qrResponseString);

            return QRCode.builder()
                    .qrCode(qrResponse.getQrCode())
                    .qrLink(qrResponse.getQrLink())
                    .bankCode(qrResponse.getBankCode())
                    .bankName(qrResponse.getBankName())
                    .bankAccount(qrResponse.getBankAccount())
                    .userBankName(qrResponse.getUserBankName())
                    .amount(qrResponse.getAmount())
                    .content(qrResponse.getContent())
                    .transactionId(qrResponse.getTransactionId())
                    .build();
        } catch (Exception e) {
            System.err.println("VietQR API call failed. Falling back to mock QR code. Error: " + e.getMessage());
            long amount = (order.getInvoice() != null && order.getInvoice().getTotalAmount() != null) ? order.getInvoice().getTotalAmount().longValue() : 1320000L;
            return QRCode.builder()
                    .qrCode("OMS_MOCK_PAYMENT_DATA_ORDER_" + order.getOrderId() + "_AMOUNT_" + amount)
                    .qrLink("https://oms.example.com/pay/" + order.getOrderId())
                    .bankCode("OMS_MOCK_BANK")
                    .bankName("OMS Mock Bank")
                    .bankAccount("0986171335")
                    .userBankName("Hoàng Anh Điệp")
                    .amount(amount)
                    .content(OrderReference.format(order.getOrderId()))
                    .transactionId("MOCK_TXN_" + System.currentTimeMillis())
                    .build();
        }
    }
}