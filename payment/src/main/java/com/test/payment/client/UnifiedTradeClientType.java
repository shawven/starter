package com.test.payment.client;

import com.test.payment.PaymentTradeClientType;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public interface UnifiedTradeClientType extends PaymentTradeClientType {
    @Override
    default PaymentClientTypeEnum getClientType() {
        return PaymentClientTypeEnum.UNIFIED;
    }
}