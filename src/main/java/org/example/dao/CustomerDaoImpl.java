package org.example.dao;

import org.example.common.JDBCUtil;
import org.example.domain.CustomerVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDaoImpl implements CustomerDao {
    Connection conn = JDBCUtil.getConnection();

    private String CUSTOMER_INSERT = "INSERT INTO 고객 (이름, 주민등록번호, 전화번호, 주소) VALUES (?, ?, ?, ?)";
    private String CUSTOMER_BY_NAME = "SELECT * FROM 고객 WHERE 이름 LIKE ?";
    private String CUSTOMER_BY_PHONE = "SELECT * FROM 고객 WHERE 전화번호 LIKE ?";
    private String CUSTOMER_BY_JOINDATE = "SELECT * FROM 고객 WHERE 가입일 LIKE ?";
    private String CUSTOMER_BY_CARDNO = "SELECT c.* FROM 고객 c JOIN 카드 k ON c.고객번호 = k.고객번호 WHERE k.카드번호 = ?";
    private String CUSTOMER_UPDATE = "UPDATE 고객 SET 전화번호 = ?, 주소 = ? WHERE 고객번호 = ?";
    private String CUSTOMER_DELETE = "DELETE FROM 고객 WHERE 고객번호 = ?";

    private CustomerVO map(ResultSet rs) throws SQLException {
        CustomerVO customer = new CustomerVO();
        customer.setCustomerNo(rs.getInt("고객번호"));
        customer.setName(rs.getString("이름"));
        customer.setSsn(rs.getString("주민등록번호"));
        customer.setPhone(rs.getString("전화번호"));
        customer.setAddress(rs.getString("주소"));
        customer.setJoinDate(rs.getString("가입일"));
        return customer;
    }

    private String CUSTOMER_CARDS = "SELECT 카드번호 FROM 카드 WHERE 고객번호 = ?";

    public String getCardNumbers(int customerNo) throws SQLException {
        List<String> cards = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_CARDS)) {
            pstmt.setInt(1, customerNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(String.valueOf(rs.getInt("카드번호")));
                }
            }
        }
        return cards.isEmpty() ? "없음" : String.join(", ", cards);
    }

    @Override
    public int create(CustomerVO customer) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_INSERT)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getSsn());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getAddress());
            return pstmt.executeUpdate();
        }
    }

    @Override
    public List<CustomerVO> getByName(String name) throws SQLException {
        List<CustomerVO> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_BY_NAME)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public List<CustomerVO> getByPhone(String phone) throws SQLException {
        List<CustomerVO> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_BY_PHONE)) {
            pstmt.setString(1, "%" + phone + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public List<CustomerVO> getByJoinDate(String joinDate) throws SQLException {
        List<CustomerVO> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_BY_JOINDATE)) {
            pstmt.setString(1, "%" + joinDate + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public CustomerVO getByCardNo(int cardNo) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_BY_CARDNO)) {
            pstmt.setInt(1, cardNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    @Override
    public int update(CustomerVO customer) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_UPDATE)) {
            pstmt.setString(1, customer.getPhone());
            pstmt.setString(2, customer.getAddress());
            pstmt.setInt(3, customer.getCustomerNo());
            return pstmt.executeUpdate();
        }
    }

    @Override
    public int delete(int customerNo) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_DELETE)) {
            pstmt.setInt(1, customerNo);
            return pstmt.executeUpdate();
        }
    }

    private String CUSTOMER_GET = "SELECT * FROM 고객 WHERE 고객번호 = ?";

    @Override
    public CustomerVO get(int customerNo) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(CUSTOMER_GET)) {
            pstmt.setInt(1, customerNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }
}