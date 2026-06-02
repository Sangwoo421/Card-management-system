package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    private final Connection conn;

    public PaymentDao(Connection conn) { this.conn = conn; }

    public static class CardInfo {
        public long  카드번호;
        public String 상품명, 카드분류, 명의자, 카드상태;
        public long  현재잔여한도;
        public java.sql.Date 유효기간;
    }
    public static class MerchantInfo {
        public long  가맹점번호;
        public String 가맹점명, 업종;
    }
    public static class CustomerInfo {
        public long  고객번호;
        public String 이름;
        public int   카드수;
    }
    public static class Product {
        public int    상품번호;
        public String 상품명, 카드분류;
    }
    public static class PaymentRow {
        public long   결제번호, 결제금액, 카드번호, 할부개월수;
        public String 결제일시, 취소일시, 결제상태;
        public String 가맹점명, 상품명, 명의자;
    }
    public static class Stats {
        public int  총건, 승인건, 취소건;
        public long 총액, 승인액, 취소액;
    }
    public static class CardUsage {
        public long 카드번호;
        public String 상품명;
        public int 건수;
        public long 합계;
    }
    public static class InstallmentStat {
        public int 할부개월수, 건수;
        public long 합계;
    }
    public static class MerchantStat {
        public String 가맹점명;
        public int 건수;
        public long 합계;
    }

    // 카드 정보 조회 (상품명·분류·명의자·잔여한도·상태·유효기간)
    public CardInfo getCardInfo(long cardNo) throws SQLException {
        String sql =
            "SELECT c.카드번호, pr.상품명, pr.카드분류, cu.이름 AS 명의자, "
          + "       c.현재잔여한도, c.카드상태, c.유효기간 "
          + "FROM 카드 c "
          + "JOIN 카드상품 pr ON c.상품번호 = pr.상품번호 "
          + "JOIN 고객 cu     ON c.고객번호 = cu.고객번호 "
          + "WHERE c.카드번호 = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cardNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                CardInfo c = new CardInfo();
                c.카드번호     = rs.getLong("카드번호");
                c.상품명       = rs.getString("상품명");
                c.카드분류     = rs.getString("카드분류");
                c.명의자       = rs.getString("명의자");
                c.현재잔여한도 = rs.getLong("현재잔여한도");
                c.카드상태     = rs.getString("카드상태");
                c.유효기간     = rs.getDate("유효기간");
                return c;
            }
        }
    }

    // 가맹점 정보 조회
    public MerchantInfo getMerchantInfo(long merchantNo) throws SQLException {
        String sql = "SELECT 가맹점번호, 가맹점명, 업종 FROM 가맹점 WHERE 가맹점번호 = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, merchantNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                MerchantInfo m = new MerchantInfo();
                m.가맹점번호 = rs.getLong("가맹점번호");
                m.가맹점명   = rs.getString("가맹점명");
                m.업종       = rs.getString("업종");
                return m;
            }
        }
    }

    // 고객 정보 + 보유 카드 수
    public CustomerInfo getCustomerInfo(long customerNo) throws SQLException {
        String sql =
            "SELECT cu.고객번호, cu.이름, COUNT(c.카드번호) AS 카드수 "
          + "FROM 고객 cu LEFT JOIN 카드 c ON c.고객번호 = cu.고객번호 "
          + "WHERE cu.고객번호 = ? GROUP BY cu.고객번호, cu.이름";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                CustomerInfo c = new CustomerInfo();
                c.고객번호 = rs.getLong("고객번호");
                c.이름     = rs.getString("이름");
                c.카드수   = rs.getInt("카드수");
                return c;
            }
        }
    }

    // 카드상품 목록
    public List<Product> findAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT 상품번호, 상품명, 카드분류 FROM 카드상품 ORDER BY 상품번호";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.상품번호 = rs.getInt("상품번호");
                p.상품명   = rs.getString("상품명");
                p.카드분류 = rs.getString("카드분류");
                list.add(p);
            }
        }
        return list;
    }

    // 카드 잔여한도만 조회
    public long getCardBalance(long cardNo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 현재잔여한도 FROM 카드 WHERE 카드번호 = ?")) {
            ps.setLong(1, cardNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return -1;
                return rs.getLong(1);
            }
        }
    }

    // 결제 단건 조회 (취소 처리 전 확인용)
    public PaymentRow getPayment(long paymentNo) throws SQLException {
        String sql =
            "SELECT p.결제번호, p.결제금액, p.카드번호, p.결제일시, p.결제상태, m.가맹점명 "
          + "FROM 결제내역 p JOIN 가맹점 m ON p.가맹점번호 = m.가맹점번호 "
          + "WHERE p.결제번호 = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                PaymentRow r = new PaymentRow();
                r.결제번호  = rs.getLong("결제번호");
                r.결제금액  = rs.getLong("결제금액");
                r.카드번호  = rs.getLong("카드번호");
                r.결제일시  = rs.getString("결제일시");
                r.결제상태  = rs.getString("결제상태");
                r.가맹점명  = rs.getString("가맹점명");
                return r;
            }
        }
    }

    // 카드별 결제내역 (기간 옵션)
    public List<PaymentRow> findByCard(long cardNo, String from, String to) throws SQLException {
        String sql =
            "SELECT p.결제번호, m.가맹점명, p.결제금액, p.할부개월수, p.결제일시, p.결제상태, p.취소일시 "
          + "FROM 결제내역 p JOIN 가맹점 m ON p.가맹점번호 = m.가맹점번호 "
          + "WHERE p.카드번호 = ? " + periodClause(from, to)
          + "ORDER BY p.결제일시 DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cardNo);
            bindPeriod(ps, 2, from, to);
            return readList(ps, false, false);
        }
    }

    // 고객별 결제내역
    public List<PaymentRow> findByCustomer(long customerNo, String from, String to) throws SQLException {
        String sql =
            "SELECT p.결제번호, p.결제일시, c.카드번호, pr.상품명, m.가맹점명, "
          + "       p.결제금액, p.할부개월수, p.결제상태, p.취소일시 "
          + "FROM 결제내역 p "
          + "JOIN 카드 c     ON p.카드번호  = c.카드번호 "
          + "JOIN 카드상품 pr ON c.상품번호 = pr.상품번호 "
          + "JOIN 가맹점 m   ON p.가맹점번호 = m.가맹점번호 "
          + "WHERE c.고객번호 = ? " + periodClause(from, to)
          + "ORDER BY p.결제일시 DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerNo);
            bindPeriod(ps, 2, from, to);
            return readList(ps, true, false);
        }
    }

    // 전체 결제내역 (상품번호 null=전체, 기간 옵션)
    public List<PaymentRow> findAll(Integer productNo, String from, String to) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT p.결제번호, p.결제일시, c.카드번호, cu.이름 AS 명의자, m.가맹점명, "
          + "       p.결제금액, p.할부개월수, p.결제상태, p.취소일시 "
          + "FROM 결제내역 p "
          + "JOIN 카드 c   ON p.카드번호   = c.카드번호 "
          + "JOIN 고객 cu  ON c.고객번호   = cu.고객번호 "
          + "JOIN 가맹점 m ON p.가맹점번호 = m.가맹점번호 "
          + "WHERE 1=1 ");
        if (productNo != null) sql.append("AND c.상품번호 = ? ");
        sql.append(periodClause(from, to)).append("ORDER BY p.결제일시 DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (productNo != null) ps.setInt(idx++, productNo);
            bindPeriod(ps, idx, from, to);
            return readList(ps, false, true);
        }
    }

    private List<PaymentRow> readList(PreparedStatement ps, boolean includeProduct, boolean includeCustomer)
            throws SQLException {
        List<PaymentRow> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PaymentRow r = new PaymentRow();
                r.결제번호   = rs.getLong("결제번호");
                r.결제금액   = rs.getLong("결제금액");
                r.결제일시   = rs.getString("결제일시");
                r.취소일시   = rs.getString("취소일시");
                r.결제상태   = rs.getString("결제상태");
                r.할부개월수 = rs.getLong("할부개월수");
                r.가맹점명   = rs.getString("가맹점명");
                if (includeProduct)  { r.카드번호 = rs.getLong("카드번호"); r.상품명 = rs.getString("상품명"); }
                if (includeCustomer) { r.카드번호 = rs.getLong("카드번호"); r.명의자 = rs.getString("명의자"); }
                list.add(r);
            }
        }
        return list;
    }

    // 결제: INSERT + 카드 한도 차감 (트랜잭션)
    public PaymentRow processPayment(long amount, int installment, long cardNo, long merchantNo)
            throws SQLException {
        conn.setAutoCommit(false);
        try {
            long newPaymentNo;
            String ins =
                "INSERT INTO 결제내역 (결제금액, 결제상태, 할부개월수, 카드번호, 가맹점번호) "
              + "VALUES (?, '승인', ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, amount);
                ps.setInt (2, installment);
                ps.setLong(3, cardNo);
                ps.setLong(4, merchantNo);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    newPaymentNo = keys.getLong(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE 카드 SET 현재잔여한도 = 현재잔여한도 - ? WHERE 카드번호 = ?")) {
                ps.setLong(1, amount);
                ps.setLong(2, cardNo);
                ps.executeUpdate();
            }
            String paidAt;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 결제일시 FROM 결제내역 WHERE 결제번호 = ?")) {
                ps.setLong(1, newPaymentNo);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); paidAt = rs.getString("결제일시"); }
            }
            conn.commit();
            PaymentRow r = new PaymentRow();
            r.결제번호 = newPaymentNo;
            r.결제일시 = paidAt;
            return r;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // 취소: 상태=취소, 취소일시 기록 + 한도 복구 (트랜잭션)
    public boolean processCancel(long paymentNo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            long amount, cardNo;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 결제금액, 카드번호 FROM 결제내역 WHERE 결제번호 = ? AND 결제상태 = '승인'")) {
                ps.setLong(1, paymentNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return false; }
                    amount = rs.getLong("결제금액");
                    cardNo = rs.getLong("카드번호");
                }
            }
            int affected;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE 결제내역 SET 결제상태='취소', 취소일시=NOW() "
                  + "WHERE 결제번호 = ? AND 결제상태 = '승인'")) {
                ps.setLong(1, paymentNo);
                affected = ps.executeUpdate();
            }
            if (affected == 0) { conn.rollback(); return false; }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE 카드 SET 현재잔여한도 = 현재잔여한도 + ? WHERE 카드번호 = ?")) {
                ps.setLong(1, amount);
                ps.setLong(2, cardNo);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // 고객별 결제 통계 (총건/총액/승인/취소)
    public Stats getStatsByCustomer(long customerNo) throws SQLException {
        String sql =
            "SELECT COUNT(*) 총건, COALESCE(SUM(결제금액),0) 총액, "
          + "       SUM(결제상태='승인') 승인건, "
          + "       COALESCE(SUM(CASE WHEN 결제상태='승인' THEN 결제금액 END),0) 승인액, "
          + "       SUM(결제상태='취소') 취소건, "
          + "       COALESCE(SUM(CASE WHEN 결제상태='취소' THEN 결제금액 END),0) 취소액 "
          + "FROM 결제내역 p JOIN 카드 c ON p.카드번호 = c.카드번호 "
          + "WHERE c.고객번호 = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerNo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Stats s = new Stats();
                s.총건   = rs.getInt("총건");
                s.총액   = rs.getLong("총액");
                s.승인건 = rs.getInt("승인건");
                s.승인액 = rs.getLong("승인액");
                s.취소건 = rs.getInt("취소건");
                s.취소액 = rs.getLong("취소액");
                return s;
            }
        }
    }

    // 카드별 사용 내역 (고객 기준)
    public List<CardUsage> getCardUsageByCustomer(long customerNo) throws SQLException {
        String sql =
            "SELECT c.카드번호, pr.상품명, COUNT(p.결제번호) 건수, COALESCE(SUM(p.결제금액),0) 합계 "
          + "FROM 카드 c JOIN 카드상품 pr ON c.상품번호 = pr.상품번호 "
          + "LEFT JOIN 결제내역 p ON p.카드번호 = c.카드번호 "
          + "WHERE c.고객번호 = ? GROUP BY c.카드번호, pr.상품명 ORDER BY 합계 DESC";
        List<CardUsage> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CardUsage u = new CardUsage();
                    u.카드번호 = rs.getLong("카드번호");
                    u.상품명   = rs.getString("상품명");
                    u.건수     = rs.getInt("건수");
                    u.합계     = rs.getLong("합계");
                    list.add(u);
                }
            }
        }
        return list;
    }

    // 할부개월수별 통계 (승인만)
    public List<InstallmentStat> getInstallmentStatsByCustomer(long customerNo) throws SQLException {
        String sql =
            "SELECT 할부개월수, COUNT(*) 건수, SUM(결제금액) 합계 "
          + "FROM 결제내역 p JOIN 카드 c ON p.카드번호 = c.카드번호 "
          + "WHERE c.고객번호 = ? AND p.결제상태 = '승인' "
          + "GROUP BY 할부개월수 ORDER BY 할부개월수";
        List<InstallmentStat> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InstallmentStat i = new InstallmentStat();
                    i.할부개월수 = rs.getInt("할부개월수");
                    i.건수       = rs.getInt("건수");
                    i.합계       = rs.getLong("합계");
                    list.add(i);
                }
            }
        }
        return list;
    }

    // 가맹점 TOP N (승인 금액 기준)
    public List<MerchantStat> getTopMerchantsByCustomer(long customerNo, int limit) throws SQLException {
        String sql =
            "SELECT m.가맹점명, COUNT(*) 건수, SUM(p.결제금액) 합계 "
          + "FROM 결제내역 p JOIN 카드 c ON p.카드번호 = c.카드번호 "
          + "JOIN 가맹점 m ON p.가맹점번호 = m.가맹점번호 "
          + "WHERE c.고객번호 = ? AND p.결제상태 = '승인' "
          + "GROUP BY m.가맹점번호, m.가맹점명 ORDER BY 합계 DESC LIMIT ?";
        List<MerchantStat> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerNo);
            ps.setInt (2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MerchantStat m = new MerchantStat();
                    m.가맹점명 = rs.getString("가맹점명");
                    m.건수     = rs.getInt("건수");
                    m.합계     = rs.getLong("합계");
                    list.add(m);
                }
            }
        }
        return list;
    }

    private static String periodClause(String from, String to) {
        return (from == null || to == null) ? "" : "AND p.결제일시 >= ? AND p.결제일시 <= ? ";
    }
    private static void bindPeriod(PreparedStatement ps, int startIdx, String from, String to) throws SQLException {
        if (from == null || to == null) return;
        ps.setString(startIdx,     from + " 00:00:00");
        ps.setString(startIdx + 1, to   + " 23:59:59");
    }
}
