package com.test.payment.support;

import com.alipay.api.AlipayApiException;
import com.test.payment.PaymentException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


/**
 * @author Shoven
 * @date 2019-09-18
 */
public class SecurityUtils {

    public static String sha1HexString(String source, Charset charset) {
        return byte2Hex(sha1(source.getBytes(charset)));
    }

    public static String sha2HexString(String source, Charset charset) {
        return byte2Hex(sha2(source.getBytes(charset)));
    }

    public static byte[] sha1(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(PaymentConstants.ALGORITHM_SHA1);
            md.update(bytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new PaymentException(e);
        }
    }

    public static byte[] sha2(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(PaymentConstants.ALGORITHM_SHA256);
            md.update(bytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new PaymentException(e);
        }
    }

    public static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

}
