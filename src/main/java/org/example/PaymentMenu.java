package org.example;

import org.example.common.JDBCUtil;
import org.example.dao.PaymentDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * 결제 처리 메뉴 (명세 7-3) — UI/입력 전담.
 *   7-3-1 결제 / 7-3-2 결제내역 조회(카드별/고객별/전체 + 취소) / 7-3-3 통계
 * SQL은 PaymentDao 가 담당. 본 클래스는 Dao 호출 + 화면 출력만.
 *
 * Main 에서 호출:   PaymentMenu.showMenu(sc);
 *   - 전달받은 Scanner 를 정적 필드에 보관해, Main 과 같은 입력 스트림 사용.
 */
public class PaymentMenu {

    private static Scanner sc;

    /** 단독 실행용 (DAO 패턴만 따로 테스트할 때) */
    public static void main(String[] args) { showMenu(new Scanner(System.in)); }

    // ===================== 메인 결제 처리 메뉴 =====================
    public static void showMenu(Scanner scanner) {
        sc = scanner;
        while (true) {
            System.out.println("\n========================================");
            System.out.println("    결제 처리");
            System.out.println("========================================");
            System.out.println("1. 결제");
            System.out.println("2. 결제내역 조회");
            System.out.println("3. 결제 통계 조회");
            System.out.println("0. 메인 메뉴로 이동");
            System.out.println("========================================");
            System.out.print(">> 메뉴 선택: ");

            switch (sc.nextLine().trim()) {
                case "1": pay(); break;
                case "2": inquiryMenu(); break;
                case "3": statistics(); break;
                case "0": return;
                default:  System.out.println("[오류] 잘못된 입력입니다.");
            }
        }
    }

    // ===================== 7-3-1 결제 =====================
    private static void pay() {
        System.out.println("\n========================================");
        System.out.println("                결제");
        System.out.println("========================================");

        Long cardId = readLong("카드번호 입력 (0 입력 시 취소): ");
        if (cardId == null || cardId == 0) { System.out.println("결제를 취소했습니다."); return; }

        Connection conn = JDBCUtil.getConnection();
        PaymentDao dao = new PaymentDao(conn);
        try {
            PaymentDao.CardInfo card = dao.getCardInfo(cardId);
            if (card == null) { System.out.println("[오류] 존재하지 않는 카드번호입니다."); return; }

            System.out.println("\n→ [카드 정보 확인]");
            System.out.println("   카드번호: " + card.카드번호);
            System.out.println("   카드종류: " + card.카드분류);
            System.out.println("   현재 잔여한도: " + won(card.현재잔여한도));
            System.out.println("   카드상태: " + card.카드상태);

            // 즉시 거절 — 정지
            if (!"정상".equals(card.카드상태)) {
                printReject("정지된 카드입니다. (카드상태: " + card.카드상태 + ")");
                pause(); return;
            }
            // 즉시 거절 — 유효기간 만료
            LocalDate exp = card.유효기간.toLocalDate();
            if (exp.isBefore(LocalDate.now())) {
                String ym = exp.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                printReject("유효기간이 만료된 카드입니다. (유효기간: " + ym + ")");
                pause(); return;
            }

            Long merchantId = readLong("\n가맹점번호 입력: ");
            if (merchantId == null) return;
            PaymentDao.MerchantInfo m = dao.getMerchantInfo(merchantId);
            if (m == null) { System.out.println("[오류] 존재하지 않는 가맹점번호입니다."); return; }
            System.out.println("\n→ [가맹점 정보 확인]");
            System.out.println("   가맹점번호: " + m.가맹점번호);
            System.out.println("   가맹점명: " + m.가맹점명);
            System.out.println("   업종: " + m.업종);

            Long amount = readLong("\n결제금액 입력 (원): ");
            if (amount == null) return;
            if (amount <= 0) { System.out.println("[오류] 결제금액은 0보다 커야 합니다."); return; }

            Long inst = readLong("할부개월수 입력 (0=일시불, 2~12): ");
            if (inst == null) return;
            if (!(inst == 0 || (inst >= 2 && inst <= 12))) {
                System.out.println("[오류] 할부개월수는 0(일시불) 또는 2~12 만 가능합니다.");
                return;
            }

            System.out.println("\n----------------------------------------");
            System.out.println("[결제 정보 확인]");
            System.out.println("카드:       " + card.카드번호 + " (" + shortType(card.카드분류) + ")");
            System.out.println("가맹점:     " + m.가맹점명);
            System.out.println("결제금액:   " + won(amount));
            System.out.println("할부:       " + installmentText(inst));
            System.out.println("----------------------------------------");

            if (amount > card.현재잔여한도) {
                System.out.println("\n[결제 거절]");
                System.out.println("✗ 결제가 거절되었습니다.");
                System.out.println("  사유: 잔여한도 부족");
                System.out.println("  결제 요청액: " + won(amount) + " / 잔여한도: " + won(card.현재잔여한도));
                pause(); return;
            }

            PaymentDao.PaymentRow result = dao.processPayment(amount, inst.intValue(), card.카드번호, m.가맹점번호);
            System.out.println("\n[결제 승인]");
            System.out.println("✓ 결제가 정상 처리되었습니다.");
            System.out.println("  결제번호: " + result.결제번호);
            System.out.println("  승인일시: " + result.결제일시);
            System.out.println("  잔여한도: " + won(card.현재잔여한도) + " → " + won(card.현재잔여한도 - amount));

        } catch (SQLException e) {
            System.out.println("[DB 오류] " + e.getMessage());
        }
        pause();
    }

    private static void printReject(String reason) {
        System.out.println("\n[결제 거절]");
        System.out.println("✗ 결제가 거절되었습니다.");
        System.out.println("  사유: " + reason);
    }

    private static String shortType(String 카드분류) {
        return 카드분류.endsWith("카드") ? 카드분류.substring(0, 카드분류.length() - 2) : 카드분류;
    }

    // ===================== 7-3-2 결제내역 조회 (서브메뉴) =====================
    private static void inquiryMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("          결제내역 조회");
            System.out.println("========================================");
            System.out.println("1. 카드별 조회 (취소 가능)");
            System.out.println("2. 고객별 조회");
            System.out.println("3. 전체 조회");
            System.out.println("0. 이전 메뉴");
            System.out.println("========================================");
            System.out.print(">> 메뉴 선택: ");
            switch (sc.nextLine().trim()) {
                case "1": inquiryByCard(); break;
                case "2": inquiryByCustomer(); break;
                case "3": inquiryAll(); break;
                case "0": return;
                default:  System.out.println("[오류] 잘못된 입력입니다.");
            }
        }
    }

    // ---- 7-3-2-1 카드별 조회 + 취소 ----
    private static void inquiryByCard() {
        Long cardId = readLong("\n카드번호 입력 (0 입력 시 취소): ");
        if (cardId == null || cardId == 0) return;

        Connection conn = JDBCUtil.getConnection();
        PaymentDao dao = new PaymentDao(conn);
        try {
            PaymentDao.CardInfo card = dao.getCardInfo(cardId);
            if (card == null) { System.out.println("[오류] 존재하지 않는 카드번호입니다."); return; }
            System.out.println("\n→ [카드 정보]");
            System.out.println("   카드번호:   " + card.카드번호);
            System.out.println("   상품명:     " + card.상품명 + " (" + card.카드분류 + ")");
            System.out.println("   명의자:     " + card.명의자);
            System.out.println("   잔여한도:   " + won(card.현재잔여한도));

            String[] period = askPeriod();
            List<PaymentDao.PaymentRow> rows = dao.findByCard(cardId,
                period == null ? null : period[0], period == null ? null : period[1]);
            printPaymentList(rows, period, false);

            Long cancelId = readLong("\n취소할 결제번호 입력 (0 입력 시 이전 메뉴): ");
            if (cancelId == null || cancelId == 0) return;

            PaymentDao.PaymentRow target = dao.getPayment(cancelId);
            if (target == null) { System.out.println("[오류] 존재하지 않는 결제번호입니다."); return; }
            if ("취소".equals(target.결제상태)) { System.out.println("[오류] 이미 취소된 결제입니다."); return; }

            System.out.println("\n----------------------------------------");
            System.out.println("[취소 정보 확인]");
            System.out.println("결제번호:   " + target.결제번호);
            System.out.println("가맹점:     " + target.가맹점명);
            System.out.println("결제금액:   " + won(target.결제금액));
            System.out.println("결제일시:   " + target.결제일시);
            System.out.println("----------------------------------------");
            System.out.print("정말 취소하시겠습니까? (y/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) { System.out.println("✗ 취소를 중단했습니다."); return; }

            boolean ok = dao.processCancel(cancelId);
            if (!ok) { System.out.println("[오류] 취소할 수 없습니다."); return; }

            long 잔여 = dao.getCardBalance(target.카드번호);
            System.out.println("\n✓ 결제가 취소되었습니다.");
            System.out.println("  결제번호 " + cancelId + ": 승인 → 취소");
            System.out.println("  잔여한도: " + won(잔여 - target.결제금액) + " → " + won(잔여) + " (복구)");

        } catch (SQLException e) {
            System.out.println("[DB 오류] " + e.getMessage());
        }
        pause();
    }

    // ---- 7-3-2-2 고객별 조회 ----
    private static void inquiryByCustomer() {
        Long customerId = readLong("\n고객번호 입력 (0 입력 시 취소): ");
        if (customerId == null || customerId == 0) return;

        Connection conn = JDBCUtil.getConnection();
        PaymentDao dao = new PaymentDao(conn);
        try {
            PaymentDao.CustomerInfo cust = dao.getCustomerInfo(customerId);
            if (cust == null) { System.out.println("[오류] 존재하지 않는 고객번호입니다."); return; }
            System.out.println("\n→ [고객 정보]");
            System.out.println("   고객번호:   " + cust.고객번호);
            System.out.println("   이름:       " + cust.이름);
            System.out.println("   보유 카드:  " + cust.카드수 + "장");

            String[] period = askPeriod();
            List<PaymentDao.PaymentRow> rows = dao.findByCustomer(customerId,
                period == null ? null : period[0], period == null ? null : period[1]);
            printPaymentList(rows, period, true);

        } catch (SQLException e) {
            System.out.println("[DB 오류] " + e.getMessage());
        }
        pause();
    }

    // ---- 7-3-2-3 전체 조회 ----
    private static void inquiryAll() {
        System.out.println("\n조회 범위 선택");
        System.out.println("1. 카드사 전체");
        System.out.println("2. 상품별");
        System.out.println("0. 이전 메뉴");
        System.out.print(">> 선택: ");
        String sel = sc.nextLine().trim();
        if (sel.equals("0")) return;
        if (!sel.equals("1") && !sel.equals("2")) { System.out.println("[오류] 잘못된 입력입니다."); return; }

        Connection conn = JDBCUtil.getConnection();
        PaymentDao dao = new PaymentDao(conn);
        try {
            Integer productNo = null;
            if (sel.equals("2")) {
                List<PaymentDao.Product> products = dao.findAllProducts();
                System.out.println("\n[카드상품 목록]");
                System.out.println("----------------------------------------");
                for (PaymentDao.Product p : products) {
                    System.out.printf("%d. %s (%s)%n", p.상품번호, p.상품명, p.카드분류);
                }
                System.out.println("----------------------------------------");
                Long input = readLong("상품번호 입력: ");
                if (input == null) return;
                productNo = input.intValue();
            }

            String[] period = askPeriod();
            List<PaymentDao.PaymentRow> rows = dao.findAll(productNo,
                period == null ? null : period[0], period == null ? null : period[1]);

            final int W = 8 + 3 + 19 + 3 + 5 + 3 + 8 + 3 + 16 + 3 + 12 + 3 + 6 + 3 + 4;
            System.out.println("\n" + dashes(W));
            System.out.println("[전체 결제내역] " + periodLabel(period));
            System.out.println(dashes(W));
            System.out.println(
                padL("결제번호", 8) + " | " + padL("결제일시", 19) + " | "
              + padL("카드",    5) + " | " + padL("고객명",    8) + " | "
              + padL("가맹점", 16) + " | " + padL("금액",     12) + " | "
              + padL("할부",   6) + " | " + padL("상태",      4));
            System.out.println(dashes(W));

            long 승인합 = 0, 취소합 = 0; int 승인건 = 0, 취소건 = 0;
            for (PaymentDao.PaymentRow r : rows) {
                boolean canceled = "취소".equals(r.결제상태);
                if (canceled) { 취소건++; 취소합 += r.결제금액; } else { 승인건++; 승인합 += r.결제금액; }
                System.out.println(
                    padR(String.valueOf(r.결제번호), 8) + " | "
                  + padL(r.결제일시, 19) + " | "
                  + padR(String.valueOf(r.카드번호), 5) + " | "
                  + padL(r.명의자, 8) + " | "
                  + padL(r.가맹점명, 16) + " | "
                  + padR(won(r.결제금액), 12) + " | "
                  + padL(installmentText(r.할부개월수), 6) + " | "
                  + padL(r.결제상태, 4));
            }
            System.out.println(dashes(W));
            System.out.printf("총 결제: %d건 / 승인 합계: %s (%d건) / 취소: %s (%d건)%n",
                rows.size(), won(승인합), 승인건, won(취소합), 취소건);

        } catch (SQLException e) {
            System.out.println("[DB 오류] " + e.getMessage());
        }
        pause();
    }

    // ===================== 7-3-3 결제 통계 조회 =====================
    private static void statistics() {
        Long customerId = readLong("\n고객번호 입력 (0 입력 시 취소): ");
        if (customerId == null || customerId == 0) return;

        Connection conn = JDBCUtil.getConnection();
        PaymentDao dao = new PaymentDao(conn);
        try {
            PaymentDao.CustomerInfo cust = dao.getCustomerInfo(customerId);
            if (cust == null) { System.out.println("[오류] 존재하지 않는 고객번호입니다."); return; }
            System.out.println("\n→ [고객 정보]");
            System.out.println("   고객번호:   " + cust.고객번호);
            System.out.println("   이름:       " + cust.이름);
            System.out.println("   보유 카드:  " + cust.카드수 + "장");

            PaymentDao.Stats s = dao.getStatsByCustomer(customerId);
            System.out.println("\n----------------------------------------");
            System.out.println("[전체 결제 통계]");
            System.out.println("----------------------------------------");
            System.out.println("총 결제 건수:     " + s.총건 + "건");
            System.out.println("총 결제 금액:     " + won(s.총액));
            System.out.println("승인 건수:        " + s.승인건 + "건 (" + won(s.승인액) + ")");
            System.out.println("취소 건수:        " + s.취소건 + "건 (" + won(s.취소액) + ")");

            int W3 = 8 + 3 + 22 + 3 + 5 + 3 + 14;
            System.out.println("\n[카드별 사용 내역]");
            System.out.println(dashes(W3));
            System.out.println(padL("카드번호", 8) + " | " + padL("상품명", 22) + " | "
                             + padL("건수",   5) + " | " + padL("합계금액", 14));
            System.out.println(dashes(W3));
            for (PaymentDao.CardUsage u : dao.getCardUsageByCustomer(customerId)) {
                System.out.println(
                    padR(String.valueOf(u.카드번호), 8) + " | "
                  + padL(u.상품명,                22) + " | "
                  + padR(u.건수 + "건",            5) + " | "
                  + padR(won(u.합계),             14));
            }

            System.out.println("\n[결제 방식별 통계] (승인 기준)");
            System.out.println("----------------------------------------");
            int 일시불건 = 0, 할부건 = 0; long 일시불액 = 0, 할부액 = 0;
            StringBuilder 할부상세 = new StringBuilder();
            for (PaymentDao.InstallmentStat i : dao.getInstallmentStatsByCustomer(customerId)) {
                if (i.할부개월수 == 0) { 일시불건 += i.건수; 일시불액 += i.합계; }
                else {
                    할부건 += i.건수; 할부액 += i.합계;
                    할부상세.append(String.format("  - %d개월 할부:    %d건 (%s)%n",
                            i.할부개월수, i.건수, won(i.합계)));
                }
            }
            System.out.println("일시불:            " + 일시불건 + "건 (" + won(일시불액) + ")");
            System.out.println("할부:              " + 할부건 + "건 (" + won(할부액) + ")");
            if (할부상세.length() > 0) System.out.print(할부상세);

            int W4 = 2 + 3 + 18 + 3 + 10 + 3 + 14;
            System.out.println("\n[가맹점 TOP 3] (승인 기준)");
            System.out.println(dashes(W4));
            System.out.println(padL("NO", 2) + " | " + padL("가맹점명", 18) + " | "
                             + padL("이용 건수", 10) + " | " + padL("합계금액", 14));
            System.out.println(dashes(W4));
            int no = 1;
            for (PaymentDao.MerchantStat ms : dao.getTopMerchantsByCustomer(customerId, 3)) {
                System.out.println(
                    padR(String.valueOf(no++), 2) + " | "
                  + padL(ms.가맹점명,                18) + " | "
                  + padR(ms.건수 + "건",            10) + " | "
                  + padR(won(ms.합계),              14));
            }

        } catch (SQLException e) {
            System.out.println("[DB 오류] " + e.getMessage());
        }
        pause();
    }

    // ===================== 공통 출력 (카드별/고객별 목록 + 합계 + 취소내역) =====================
    private static void printPaymentList(List<PaymentDao.PaymentRow> rows, String[] period, boolean includeCardCol) {
        final int W;
        String header;
        if (includeCardCol) {
            W = 8 + 3 + 19 + 3 + 5 + 3 + 22 + 3 + 16 + 3 + 12 + 3 + 6 + 3 + 4;
            header = padL("결제번호", 8) + " | " + padL("결제일시", 19) + " | "
                   + padL("카드",  5) + " | " + padL("상품명",  22) + " | "
                   + padL("가맹점", 16) + " | " + padL("금액",   12) + " | "
                   + padL("할부",   6) + " | " + padL("상태",    4);
        } else {
            W = 8 + 3 + 16 + 3 + 12 + 3 + 6 + 3 + 19 + 3 + 4;
            header = padL("결제번호", 8) + " | " + padL("가맹점명", 16) + " | "
                   + padL("금액",   12) + " | " + padL("할부",    6) + " | "
                   + padL("결제일시", 19) + " | " + padL("상태",    4);
        }
        System.out.println("\n" + dashes(W));
        System.out.println("[결제내역] " + periodLabel(period));
        System.out.println(dashes(W));
        System.out.println(header);
        System.out.println(dashes(W));

        long 승인합 = 0, 취소합 = 0; int 승인건 = 0, 취소건 = 0;
        StringBuilder 취소내역 = new StringBuilder();

        for (PaymentDao.PaymentRow r : rows) {
            boolean canceled = "취소".equals(r.결제상태);
            if (canceled) { 취소건++; 취소합 += r.결제금액; } else { 승인건++; 승인합 += r.결제금액; }

            String pn   = String.valueOf(r.결제번호);
            String 금액 = won(r.결제금액);
            String 할부 = installmentText(r.할부개월수);

            if (includeCardCol) {
                System.out.println(
                    padR(pn, 8) + " | " + padL(r.결제일시, 19) + " | "
                  + padR(String.valueOf(r.카드번호), 5) + " | " + padL(r.상품명, 22) + " | "
                  + padL(r.가맹점명, 16) + " | " + padR(금액, 12) + " | "
                  + padL(할부, 6) + " | " + padL(r.결제상태, 4));
            } else {
                System.out.println(
                    padR(pn, 8) + " | " + padL(r.가맹점명, 16) + " | "
                  + padR(금액, 12) + " | " + padL(할부, 6) + " | "
                  + padL(r.결제일시, 19) + " | " + padL(r.결제상태, 4));
            }
            if (canceled) {
                취소내역.append(
                    padR(pn, 8) + " | " + padL(r.가맹점명, 16) + " | "
                  + padR(금액, 12) + " | " + padL(r.결제일시, 19) + " | "
                  + padL(r.취소일시 == null ? "" : r.취소일시, 19))
                  .append('\n');
            }
        }
        System.out.println(dashes(W));
        System.out.printf("총 결제: %d건 / 승인 합계: %s (%d건) / 취소: %s (%d건)%n",
            rows.size(), won(승인합), 승인건, won(취소합), 취소건);

        if (취소내역.length() > 0) {
            int W2 = 8 + 3 + 16 + 3 + 12 + 3 + 19 + 3 + 19;
            System.out.println("\n[취소 내역]");
            System.out.println(dashes(W2));
            System.out.println(padL("결제번호", 8) + " | " + padL("가맹점명", 16) + " | "
                             + padL("금액",   12) + " | " + padL("결제일시", 19) + " | "
                             + padL("취소일시", 19));
            System.out.println(dashes(W2));
            System.out.print(취소내역);
            System.out.println(dashes(W2));
        }
    }

    // ===================== 입력/포맷 헬퍼 =====================

    private static int dispWidth(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            w += isWide(cp) ? 2 : 1;
            i += Character.charCount(cp);
        }
        return w;
    }
    private static boolean isWide(int cp) {
        return (cp >= 0x1100 && cp <= 0x115F)
            || (cp >= 0x2E80 && cp <= 0x303E)
            || (cp >= 0x3041 && cp <= 0x33FF)
            || (cp >= 0x3400 && cp <= 0x4DBF)
            || (cp >= 0x4E00 && cp <= 0x9FFF)
            || (cp >= 0xAC00 && cp <= 0xD7A3)
            || (cp >= 0xF900 && cp <= 0xFAFF)
            || (cp >= 0xFE30 && cp <= 0xFE4F)
            || (cp >= 0xFF00 && cp <= 0xFF60)
            || (cp >= 0xFFE0 && cp <= 0xFFE6);
    }
    private static String padL(String s, int width) {
        if (s == null) s = "";
        int diff = width - dispWidth(s);
        if (diff <= 0) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < diff; i++) sb.append(' ');
        return sb.toString();
    }
    private static String padR(String s, int width) {
        if (s == null) s = "";
        int diff = width - dispWidth(s);
        if (diff <= 0) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diff; i++) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }
    private static String dashes(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('-');
        return sb.toString();
    }

    private static Long readLong(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try { return Long.parseLong(line); }
        catch (NumberFormatException e) { System.out.println("[오류] 숫자를 입력해 주세요."); return null; }
    }

    private static String[] askPeriod() {
        System.out.print("\n기간 설정 (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("전체 기간으로 조회합니다.");
            return null;
        }
        System.out.println("  ┌─────────────────────────────────────────────┐");
        System.out.println("  │  입력 형식: YYYYMMDD   (하이픈 생략 가능)    │");
        System.out.println("  │  예시:      20260501  (= 2026-05-01)         │");
        System.out.println("  │  종료일은 엔터만 치면 오늘로 설정됨           │");
        System.out.println("  └─────────────────────────────────────────────┘");
        LocalDate start = readDate("  시작일 입력: ", null);
        LocalDate end   = readDate("  종료일 입력: ", LocalDate.now());
        if (end.isBefore(start)) {
            System.out.println("  [알림] 종료일이 시작일보다 이전입니다. 두 날짜를 바꿔서 조회합니다.");
            LocalDate t = start; start = end; end = t;
        }
        System.out.println("  → 조회 기간: " + start + " ~ " + end);
        return new String[]{start.toString(), end.toString()};
    }

    private static LocalDate readDate(String prompt, LocalDate defaultIfEmpty) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty() && defaultIfEmpty != null) return defaultIfEmpty;
            String digits = s.replace("-", "");
            try {
                if (digits.length() == 8 && digits.chars().allMatch(Character::isDigit)) {
                    return LocalDate.parse(digits, DateTimeFormatter.ofPattern("yyyyMMdd"));
                }
                return LocalDate.parse(s);
            } catch (Exception e) {
                System.out.println("  [오류] 형식이 올바르지 않습니다. 예: 20260501 또는 2026-05-01");
            }
        }
    }

    private static String periodLabel(String[] p) {
        return (p == null) ? "(전체 기간)" : "(" + p[0] + " ~ " + p[1] + ")";
    }
    private static String installmentText(long month) {
        return (month == 0) ? "일시불" : month + "개월";
    }
    private static String won(long amount) {
        return String.format("%,d원", amount);
    }
    private static void pause() {
        System.out.print("\nEnter 키를 누르면 메뉴로 돌아갑니다.");
        sc.nextLine();
    }
}
