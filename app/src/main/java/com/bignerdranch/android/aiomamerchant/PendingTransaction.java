package com.bignerdranch.android.aiomamerchant;

import java.util.Comparator;

public class PendingTransaction {

    public String transactionID;
    public String merchantID;
    public String merchantName;
    public String points;



    public PendingTransaction() {
    }

    public PendingTransaction(String transactionID, String merchantID, String merchantName, String points) {
        this.transactionID = transactionID;
        this.merchantID = merchantID;
        this.merchantName = merchantName;
        this.points = points;


    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }


    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }


    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }
}


