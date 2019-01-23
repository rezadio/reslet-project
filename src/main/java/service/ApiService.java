package service;

import apigatewayottopay.entity.*;
import org.json.simple.JSONObject;

/**
 * @author Reza Dio Nugraha
 */
public interface ApiService {
    InquiryResponse getInquiry(InquiryRequest request, JSONObject jsonReq);
    PaymentResponse getPayment(BaseApiRequest baseApiRequest, JSONObject jsonReq);
    ReversePaymentResponse getReverse(ReversePaymentRequest request, JSONObject jsonReq);
    ReversePaymentResponse getRefund(RefundPaymentRequest request, JSONObject jsonReq);
}
