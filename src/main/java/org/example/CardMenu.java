package org.example;

import org.example.common.JDBCUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class CardMenu {
    private final Scanner sc;
    private final Connection conn;

    public CardMenu(Scanner sc) {
        this.sc = sc;
        this.conn = JDBCUtil.getConnection();
    }

    public void cardMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("             카드 관리");
            System.out.println("========================================");
            System.out.println("1. 고객 검색 및 보유 카드 조회");
            System.out.println("2. 카드 발급");
            System.out.println("3. 카드 상태 변경");
            System.out.println("4. 카드 한도 변경");
            System.out.println("0. 메인 메뉴 이동");
            System.out.println("========================================");
            System.out.print("선택: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    searchCustomerAndCards();
                    break;
                case 2:
                    issueCard();
                    break;
                case 3:
                    updateCardStatus();
                    break;
                case 4:
                    updateCardLimit();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    public void cardProductMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("           카드상품 관리");
            System.out.println("========================================");
            System.out.println("1. 카드상품 등록");
            System.out.println("2. 전체 카드상품 조회");
            System.out.println("3. 카드상품 정보 수정");
            System.out.println("4. 카드상품 삭제");
            System.out.println("0. 메인 메뉴 이동");
            System.out.println("========================================");
            System.out.print("선택: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    insertCardProduct();
                    break;
                case 2:
                    selectCardProducts();
                    break;
                case 3:
                    updateCardProduct();
                    break;
                case 4:
                    deleteCardProduct();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private void searchCustomerAndCards() {
        try {
            int customerId = searchCustomer();

            if (customerId == -1) {
                return;
            }

            String customerSql = """
                    SELECT 고객번호, 이름, 전화번호, 주소
                    FROM 고객
                    WHERE 고객번호 = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(customerSql)) {
                pstmt.setInt(1, customerId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("\n[고객 정보]");
                        System.out.println("고객번호: " + rs.getInt("고객번호"));
                        System.out.println("이름: " + rs.getString("이름"));
                        System.out.println("전화번호: " + rs.getString("전화번호"));
                        System.out.println("주소: " + rs.getString("주소"));
                    }
                }
            }

            printCustomerCards(customerId);

        } catch (SQLException e) {
            System.out.println("고객 카드 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void issueCard() {
        try {
            int customerId = searchCustomer();

            if (customerId == -1) {
                return;
            }

            System.out.println("\n[카드상품 목록]");
            selectCardProductsSimple();

            System.out.print("상품번호 선택: ");
            int productId = sc.nextInt();
            sc.nextLine();

            System.out.print("카드한도 입력: ");
            BigDecimal cardLimit = new BigDecimal(sc.nextLine());

            System.out.print("현재잔여한도 입력: ");
            BigDecimal remainLimit = new BigDecimal(sc.nextLine());

            System.out.print("유효기간 입력 예시(2029-05-31): ");
            Date expiryDate = Date.valueOf(sc.nextLine());

            System.out.print("카드상태 입력(정상/정지/해지): ");
            String cardStatus = sc.nextLine();

            String sql = """
                    INSERT INTO 카드 (
                        카드한도,
                        현재잔여한도,
                        유효기간,
                        카드상태,
                        고객번호,
                        상품번호
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setBigDecimal(1, cardLimit);
                pstmt.setBigDecimal(2, remainLimit);
                pstmt.setDate(3, expiryDate);
                pstmt.setString(4, cardStatus);
                pstmt.setInt(5, customerId);
                pstmt.setInt(6, productId);

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int cardId = rs.getInt(1);

                            System.out.println("\n[발급 완료]");
                            System.out.println("카드번호: " + cardId);
                            System.out.println("고객번호: " + customerId);
                            System.out.println("상품번호: " + productId);
                            System.out.println("카드상태: " + cardStatus);
                        }
                    }
                } else {
                    System.out.println("카드 발급 실패");
                }
            }

        } catch (Exception e) {
            System.out.println("카드 발급 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void updateCardStatus() {
        try {
            int customerId = searchCustomer();

            if (customerId == -1) {
                return;
            }

            printCustomerCards(customerId);

            System.out.print("상태를 변경할 카드번호 선택: ");
            int cardId = sc.nextInt();
            sc.nextLine();

            System.out.println("\n변경할 상태 선택");
            System.out.println("1. 정상");
            System.out.println("2. 정지");
            System.out.println("3. 해지");
            System.out.print("선택: ");

            int statusChoice = sc.nextInt();
            sc.nextLine();

            String newStatus;

            switch (statusChoice) {
                case 1:
                    newStatus = "정상";
                    break;
                case 2:
                    newStatus = "정지";
                    break;
                case 3:
                    newStatus = "해지";
                    break;
                default:
                    System.out.println("잘못된 입력입니다.");
                    return;
            }

            String sql = "UPDATE 카드 SET 카드상태 = ? WHERE 카드번호 = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, cardId);

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    System.out.println("\n[변경 완료]");
                    System.out.println("카드번호: " + cardId);
                    System.out.println("카드상태: " + newStatus);
                } else {
                    System.out.println("변경 실패");
                }
            }

        } catch (Exception e) {
            System.out.println("카드 상태 변경 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void updateCardLimit() {
        try {
            int customerId = searchCustomer();

            if (customerId == -1) {
                return;
            }

            printCustomerCards(customerId);

            System.out.print("한도를 변경할 카드번호 선택: ");
            int cardId = sc.nextInt();
            sc.nextLine();

            String selectSql = """
                    SELECT 카드한도, 현재잔여한도
                    FROM 카드
                    WHERE 카드번호 = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, cardId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("\n[현재 한도 정보]");
                        System.out.println("현재 카드한도: " + rs.getBigDecimal("카드한도"));
                        System.out.println("현재 잔여한도: " + rs.getBigDecimal("현재잔여한도"));
                    }
                }
            }

            System.out.print("변경할 카드한도 입력: ");
            BigDecimal newLimit = new BigDecimal(sc.nextLine());

            String updateSql = """
                    UPDATE 카드
                    SET 카드한도 = ?, 현재잔여한도 = ?
                    WHERE 카드번호 = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setBigDecimal(1, newLimit);
                pstmt.setBigDecimal(2, newLimit);
                pstmt.setInt(3, cardId);

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    System.out.println("\n[변경 완료]");
                    System.out.println("카드번호: " + cardId);
                    System.out.println("변경된 카드한도: " + newLimit);
                    System.out.println("변경된 현재잔여한도: " + newLimit);
                } else {
                    System.out.println("변경 실패");
                }
            }

        } catch (Exception e) {
            System.out.println("카드 한도 변경 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void insertCardProduct() {
        try {
            System.out.println("\n========================================");
            System.out.println("           카드상품 등록");
            System.out.println("========================================");

            System.out.print("상품명 입력: ");
            String productName = sc.nextLine();

            System.out.print("카드분류 입력(신용카드/체크카드): ");
            String cardType = sc.nextLine();

            System.out.print("연회비 입력: ");
            BigDecimal annualFee = new BigDecimal(sc.nextLine());

            System.out.print("기본한도 입력: ");
            BigDecimal defaultLimit = new BigDecimal(sc.nextLine());

            String sql = """
                    INSERT INTO 카드상품 (상품명, 카드분류, 연회비, 기본한도)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, productName);
                pstmt.setString(2, cardType);
                pstmt.setBigDecimal(3, annualFee);
                pstmt.setBigDecimal(4, defaultLimit);

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int productId = rs.getInt(1);

                            System.out.println("\n[등록 완료]");
                            System.out.println("상품번호: " + productId);
                            System.out.println("상품명: " + productName);
                            System.out.println("카드분류: " + cardType);
                            System.out.println("연회비: " + annualFee);
                            System.out.println("기본한도: " + defaultLimit);
                        }
                    }
                } else {
                    System.out.println("등록 실패");
                }
            }

        } catch (Exception e) {
            System.out.println("카드상품 등록 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void selectCardProducts() {
        try {
            System.out.println("\n========================================");
            System.out.println("         전체 카드상품 조회");
            System.out.println("========================================");

            selectCardProductsSimple();

        } catch (SQLException e) {
            System.out.println("카드상품 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void updateCardProduct() {
        try {
            System.out.println("\n========================================");
            System.out.println("        카드상품 정보 수정");
            System.out.println("========================================");

            selectCardProductsSimple();

            System.out.print("수정할 상품번호 입력: ");
            int productId = sc.nextInt();
            sc.nextLine();

            String selectSql = """
                    SELECT 상품명, 카드분류, 연회비, 기본한도
                    FROM 카드상품
                    WHERE 상품번호 = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, productId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("\n[현재 상품 정보]");
                        System.out.println("상품명: " + rs.getString("상품명"));
                        System.out.println("카드분류: " + rs.getString("카드분류"));
                        System.out.println("연회비: " + rs.getBigDecimal("연회비"));
                        System.out.println("기본한도: " + rs.getBigDecimal("기본한도"));
                    } else {
                        System.out.println("해당 상품이 존재하지 않습니다.");
                        return;
                    }
                }
            }

            System.out.print("새 상품명 입력: ");
            String newName = sc.nextLine();

            System.out.print("새 카드분류 입력: ");
            String newType = sc.nextLine();

            System.out.print("새 연회비 입력: ");
            BigDecimal newFee = new BigDecimal(sc.nextLine());

            System.out.print("새 기본한도 입력: ");
            BigDecimal newLimit = new BigDecimal(sc.nextLine());

            String updateSql = """
                    UPDATE 카드상품
                    SET 상품명 = ?, 카드분류 = ?, 연회비 = ?, 기본한도 = ?
                    WHERE 상품번호 = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, newType);
                pstmt.setBigDecimal(3, newFee);
                pstmt.setBigDecimal(4, newLimit);
                pstmt.setInt(5, productId);

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    System.out.println("\n[수정 완료]");
                    System.out.println("상품번호: " + productId);
                    System.out.println("상품명: " + newName);
                    System.out.println("카드분류: " + newType);
                    System.out.println("연회비: " + newFee);
                    System.out.println("기본한도: " + newLimit);
                } else {
                    System.out.println("수정 실패");
                }
            }

        } catch (Exception e) {
            System.out.println("카드상품 수정 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void deleteCardProduct() {
        try {
            System.out.println("\n========================================");
            System.out.println("          카드상품 삭제");
            System.out.println("========================================");

            selectCardProductsSimple();

            System.out.print("삭제할 상품번호 입력: ");
            int productId = sc.nextInt();
            sc.nextLine();

            String checkSql = "SELECT COUNT(*) AS 발급카드수 FROM 카드 WHERE 상품번호 = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, productId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int issuedCardCount = rs.getInt("발급카드수");

                        if (issuedCardCount > 0) {
                            System.out.println("이미 발급된 카드가 있는 상품은 삭제할 수 없습니다.");
                            System.out.println("발급된 카드 수: " + issuedCardCount);
                            return;
                        }
                    }
                }
            }

            System.out.print("정말 삭제하시겠습니까? (Y/N): ");
            String answer = sc.nextLine();

            if (!answer.equalsIgnoreCase("Y")) {
                System.out.println("삭제가 취소되었습니다.");
                return;
            }

            String deleteSql = "DELETE FROM 카드상품 WHERE 상품번호 = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, productId);

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    System.out.println("[삭제 완료] 상품번호: " + productId);
                } else {
                    System.out.println("삭제 실패");
                }
            }

        } catch (Exception e) {
            System.out.println("카드상품 삭제 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private int searchCustomer() throws SQLException {
        System.out.println("\n========================================");
        System.out.println("             고객 검색");
        System.out.println("========================================");
        System.out.println("1. 이름");
        System.out.println("2. 전화번호");
        System.out.println("3. 주민등록번호");
        System.out.println("0. 취소");
        System.out.println("========================================");
        System.out.print("선택: ");

        int choice = sc.nextInt();
        sc.nextLine();

        String column;

        switch (choice) {
            case 1:
                column = "이름";
                break;
            case 2:
                column = "전화번호";
                break;
            case 3:
                column = "주민등록번호";
                break;
            case 0:
                return -1;
            default:
                System.out.println("잘못된 입력입니다.");
                return -1;
        }

        System.out.print(column + " 입력: ");
        String keyword = sc.nextLine();

        String sql = "SELECT 고객번호, 이름, 전화번호, 주소 FROM 고객 WHERE " + column + " = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n[고객 검색 결과]");
                System.out.println("고객번호 | 이름 | 전화번호 | 주소");
                System.out.println("------------------------------------------------");

                boolean found = false;

                while (rs.next()) {
                    found = true;
                    System.out.println(
                            rs.getInt("고객번호") + " | " +
                                    rs.getString("이름") + " | " +
                                    rs.getString("전화번호") + " | " +
                                    rs.getString("주소")
                    );
                }

                if (!found) {
                    System.out.println("검색 결과가 없습니다.");
                    return -1;
                }
            }
        }

        System.out.print("고객번호 선택: ");
        int customerId = sc.nextInt();
        sc.nextLine();

        return customerId;
    }

    private void printCustomerCards(int customerId) throws SQLException {
        String sql = """
                SELECT
                    c.카드번호,
                    p.상품명,
                    p.카드분류,
                    c.카드상태,
                    c.카드한도,
                    c.현재잔여한도,
                    c.유효기간
                FROM 카드 c
                JOIN 카드상품 p ON c.상품번호 = p.상품번호
                WHERE c.고객번호 = ?
                ORDER BY c.카드번호
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n[보유 카드 목록]");
                System.out.println("카드번호 | 상품명 | 카드분류 | 카드상태 | 카드한도 | 현재잔여한도 | 유효기간");
                System.out.println("--------------------------------------------------------------------------");

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.println(
                            rs.getInt("카드번호") + " | " +
                                    rs.getString("상품명") + " | " +
                                    rs.getString("카드분류") + " | " +
                                    rs.getString("카드상태") + " | " +
                                    rs.getBigDecimal("카드한도") + " | " +
                                    rs.getBigDecimal("현재잔여한도") + " | " +
                                    rs.getDate("유효기간")
                    );
                }

                if (!found) {
                    System.out.println("보유 카드가 없습니다.");
                }
            }
        }
    }

    private void selectCardProductsSimple() throws SQLException {
        String sql = """
                SELECT 상품번호, 상품명, 카드분류, 연회비, 기본한도
                FROM 카드상품
                ORDER BY 상품번호
                """;

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            System.out.println("상품번호 | 상품명 | 카드분류 | 연회비 | 기본한도");
            System.out.println("----------------------------------------------------------");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("상품번호") + " | " +
                                rs.getString("상품명") + " | " +
                                rs.getString("카드분류") + " | " +
                                rs.getBigDecimal("연회비") + " | " +
                                rs.getBigDecimal("기본한도")
                );
            }
        }
    }
}
