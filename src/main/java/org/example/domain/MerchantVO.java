package org.example.domain;

public class MerchantVO {
    private int merchantNo;
    private String merchantName;
    private String businessType;
    private String address;
    private String phone;

    public MerchantVO() {
    }

    public MerchantVO(String merchantName, String businessType, String address, String phone) {
        this.merchantName = merchantName;
        this.businessType = businessType;
        this.address = address;
        this.phone = phone;
    }

    public int getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(int merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
