package com.egzosn.pay.spring.boot.core.provider.merchant.platform;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import com.egzosn.pay.common.api.PayConfigStorage;
import com.egzosn.pay.common.api.PayService;
import com.egzosn.pay.common.bean.CertStoreType;
import com.egzosn.pay.common.bean.TransactionType;
import com.egzosn.pay.common.http.HttpConfigStorage;
import com.egzosn.pay.common.util.str.StringUtils;
import com.egzosn.pay.spring.boot.core.merchant.PaymentPlatform;
import com.egzosn.pay.spring.boot.core.merchant.bean.CommonPaymentPlatformMerchantDetails;
import com.egzosn.pay.wx.api.WxPayConfigStorage;
import com.egzosn.pay.wx.api.WxPayService;
import com.egzosn.pay.wx.bean.WxTransactionType;

/**
 * 微信支付平台
 *
 * @author egan
 * <pre>
 *                 email egzosn@gmail.com
 *                 date  2019/4/4 14:35.
 *                 </pre>
 */
@Configuration(WxPaymentPlatform.PLATFORM_NAME)
@ConditionalOnMissingBean(WxPaymentPlatform.class)
@ConditionalOnClass(name = "com.egzosn.pay.wx.api.WxPayConfigStorage")
public class WxPaymentPlatform extends WxPayConfigStorage implements PaymentPlatform {

    protected final Log LOG = LogFactory.getLog(WxPaymentPlatform.class);

    public static final String PLATFORM_NAME = "wxPay";
    @Deprecated
    public static final String platformName = PLATFORM_NAME;


    /**
     * 获取商户平台
     *
     * @return 商户平台
     */
    @Override
    public String getPlatform() {
        return PLATFORM_NAME;
    }

    /**
     * 获取支付平台对应的支付服务
     *
     * @param payConfigStorage 支付配置
     * @return 支付服务
     */
    @Override
    public PayService getPayService(PayConfigStorage payConfigStorage) {
        return getPayService(payConfigStorage, null);
    }

    /**
     * 获取支付平台对应的支付服务
     *
     * @param payConfigStorage  支付配置
     * @param httpConfigStorage 网络配置
     * @return 支付服务
     */
    @Override
    public PayService getPayService(PayConfigStorage payConfigStorage, HttpConfigStorage httpConfigStorage) {
        if (payConfigStorage instanceof WxPayConfigStorage) {
            WxPayService wxPayService = new WxPayService((WxPayConfigStorage) payConfigStorage);
            wxPayService.setRequestTemplateConfigStorage(httpConfigStorage);
            return wxPayService;
        }
        WxPayConfigStorage configStorage = new WxPayConfigStorage();
        configStorage.setInputCharset(payConfigStorage.getInputCharset());
        configStorage.setAppId(payConfigStorage.getAppId());
        configStorage.setMchId(payConfigStorage.getPid());
        configStorage.setAttach(payConfigStorage.getAttach());
        configStorage.setKeyPrivate(payConfigStorage.getKeyPrivate());
        configStorage.setKeyPublic(payConfigStorage.getKeyPublic());
        configStorage.setNotifyUrl(payConfigStorage.getNotifyUrl());
        configStorage.setReturnUrl(payConfigStorage.getReturnUrl());
        configStorage.setPayType(payConfigStorage.getPayType());
        configStorage.setTest(payConfigStorage.isTest());
        configStorage.setSignType(payConfigStorage.getSignType());

        if (payConfigStorage instanceof CommonPaymentPlatformMerchantDetails) {
            CommonPaymentPlatformMerchantDetails merchantDetails = (CommonPaymentPlatformMerchantDetails) payConfigStorage;
            configStorage.setSubAppId(merchantDetails.getSubAppId());
            configStorage.setSubMchId(merchantDetails.getSubMchId());
            if (null != merchantDetails.getKeyCert()) {
                if (null == httpConfigStorage) {
                    httpConfigStorage = new HttpConfigStorage();
                }
                httpConfigStorage.setCertStoreType(merchantDetails.getCertStoreType());
                try {
                    httpConfigStorage.setKeystore(merchantDetails.getKeyCertInputStream());
                }
                catch (IOException e) {
                    LOG.error(e);
                }

                httpConfigStorage.setCertStoreType(CertStoreType.INPUT_STREAM);
                httpConfigStorage.setStorePassword(merchantDetails.getKeystorePwd());

            }
        }


        WxPayService wxPayService = new WxPayService(configStorage);
        wxPayService.setRequestTemplateConfigStorage(httpConfigStorage);
        return wxPayService;
    }

    @Override
    public TransactionType getTransactionType(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return WxTransactionType.valueOf(name);
    }


}
