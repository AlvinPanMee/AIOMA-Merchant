package com.bignerdranch.android.aiomamerchant;

public class Merchant {
    public String merchantID;
    public String merchantName;
    public String email;
    public String type;
    public String merchantPFPUrl;

    public Merchant(String s){

    }

    public Merchant(String merchantID, String merchantName, String email, String merchantType, String merchantPFPUrl){
        this.merchantID = merchantID;
        this.merchantName = merchantName;
        this.email = email;
        this.type = merchantType;
        this.merchantPFPUrl = merchantPFPUrl;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantPFPUrl() {
        return merchantPFPUrl;
    }

    public void setMerchantPFPUrl(String merchantPFPUrl) {
        this.merchantPFPUrl = merchantPFPUrl;
    }
}
