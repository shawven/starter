package com.test.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public class PaymentTradeRefundQueryRequest extends PaymentRequest {

    /**
     * 支付交易号
     */
    private String outTradeNo;

    private Object option;

    public PaymentTradeRefundQueryRequest(PaymentRequest request) {
        super(request);
    }

    public PaymentTradeRefundQueryRequest(String paymentSupplier) {
        super(paymentSupplier);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Object getOption() {
        return option;
    }

    public void setOption(Object option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return "PaymentTradeQueryRequest{" +
                ", platformTradeNo='" + outTradeNo + '\'' +
                ", option=" + option +
                '}';
    }
}
