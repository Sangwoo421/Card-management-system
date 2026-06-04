package org.example.dao;

import org.example.common.JDBCUtil;
import org.example.domain.MerchantVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MerchantDaoImpl implements MerchantDao {
    Connection conn = JDBCUtil.getConnection();

    private String MERCHANT_INSERT = "INSERT INTO 가맹점 (가맹점명, 업종, 가맹점주소, 전화번호) VALUES (?, ?, ?, ?)";
    private String MERCHANT_ALL = "SELECT * FROM 가맹점 ORDER BY 가맹점번호";
    private String MERCHANT_GET = "SELECT * FROM 가맹점 WHERE 가맹점번호 = ?";
    private String MERCHANT_UPDATE_NAME = "UPDATE 가맹점 SET 가맹점명 = ? WHERE 가맹점번호 = ?";
    private String MERCHANT_UPDATE_BUSINESS_TYPE = "UPDATE 가맹점 SET 업종 = ? WHERE 가맹점번호 = ?";
    private String MERCHANT_UPDATE_ADDRESS = "UPDATE 가맹점 SET 가맹점주소 = ? WHERE 가맹점번호 = ?";
    private String MERCHANT_UPDATE_PHONE = "UPDATE 가맹점 SET 전화번호 = ? WHERE 가맹점번호 = ?";
    private String MERCHANT_DELETE = "DELETE FROM 가맹점 WHERE 가맹점번호 = ?";

    private MerchantVO map(ResultSet rs) throws SQLException {
        MerchantVO merchant = new MerchantVO();
        merchant.setMerchantNo(rs.getInt("가맹점번호"));
        merchant.setMerchantName(rs.getString("가맹점명"));
        merchant.setBusinessType(rs.getString("업종"));
        merchant.setAddress(rs.getString("가맹점주소"));
        merchant.setPhone(rs.getString("전화번호"));
        return merchant;
    }

    @Override
    public MerchantVO create(MerchantVO merchant) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(MERCHANT_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, merchant.getMerchantName());
            pstmt.setString(2, merchant.getBusinessType());
            pstmt.setString(3, merchant.getAddress());
            pstmt.setString(4, merchant.getPhone());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    merchant.setMerchantNo(rs.getInt(1));
                }
            }
        }
        return merchant;
    }

    @Override
    public List<MerchantVO> getAll() throws SQLException {
        List<MerchantVO> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(MERCHANT_ALL)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public MerchantVO get(int merchantNo) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(MERCHANT_GET)) {
            pstmt.setInt(1, merchantNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    @Override
    public int updateName(int merchantNo, String merchantName) throws SQLException {
        return update(MERCHANT_UPDATE_NAME, merchantNo, merchantName);
    }

    @Override
    public int updateBusinessType(int merchantNo, String businessType) throws SQLException {
        return update(MERCHANT_UPDATE_BUSINESS_TYPE, merchantNo, businessType);
    }

    @Override
    public int updateAddress(int merchantNo, String address) throws SQLException {
        return update(MERCHANT_UPDATE_ADDRESS, merchantNo, address);
    }

    @Override
    public int updatePhone(int merchantNo, String phone) throws SQLException {
        return update(MERCHANT_UPDATE_PHONE, merchantNo, phone);
    }

    @Override
    public int delete(int merchantNo) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(MERCHANT_DELETE)) {
            pstmt.setInt(1, merchantNo);
            return pstmt.executeUpdate();
        }
    }

    private int update(String sql, int merchantNo, String value) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setInt(2, merchantNo);
            return pstmt.executeUpdate();
        }
    }
}
