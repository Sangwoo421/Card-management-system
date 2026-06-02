package org.example.domain;

public class CustomerVO {
    private int customerNo;
    private String name;
    private String ssn;
    private String phone;
    private String address;
    private String joinDate;

    public CustomerVO() {}

    public CustomerVO(String name, String ssn, String phone, String address) {
        this.name = name;
        this.ssn = ssn;
        this.phone = phone;
        this.address = address;
    }

    public int getCustomerNo() { return customerNo; }
    public void setCustomerNo(int customerNo) { this.customerNo = customerNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    @Override
    public String toString() {
        return customerNo + " | " + name + " | " + phone + " | " + joinDate;
    }
}