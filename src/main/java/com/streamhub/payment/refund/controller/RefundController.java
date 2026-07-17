package com.streamhub.payment.refund.controller;

import com.streamhub.payment.refund.dto.request.CreateRefundRequest;
import com.streamhub.payment.refund.dto.response.CreateRefundResponse;
import com.streamhub.payment.refund.dto.response.RefundResponse;
import com.streamhub.payment.refund.dto.response.RefundSummaryResponse;
import com.streamhub.payment.refund.service.RefundService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<CreateRefundResponse> createRefund(@Valid @RequestBody CreateRefundRequest request){
        CreateRefundResponse response = refundService.createRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{refundReference}")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable @NotNull(message = "refund reference is required") String refundReference){
        RefundResponse response =refundService.getRefund(refundReference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/{paymentReference}")
    public ResponseEntity<List<RefundSummaryResponse>> getRefundsByPayment(
            @PathVariable String paymentReference){
        List<RefundSummaryResponse> response =
                refundService.getRefundsByPayment(paymentReference);
        return ResponseEntity.ok(response);
    }
}
