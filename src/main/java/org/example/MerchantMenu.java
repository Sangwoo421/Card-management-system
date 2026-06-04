package org.example;

import org.example.dao.MerchantDao;
import org.example.dao.MerchantDaoImpl;
import org.example.domain.MerchantVO;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MerchantMenu {
    static Scanner sc = Main.sc;
    static MerchantDao merchantDao = new MerchantDaoImpl();

    static void merchantMenu() {
        while (true) {
            printMenu();

            int choice = readInt("선택: ");
            switch (choice) {
                case 1:
                    insertMerchant();
                    break;
                case 2:
                    selectMerchants();
                    break;
                case 3:
                    manageMerchant();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("[오류] 잘못된 입력입니다.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("          가맹점 관리");
        System.out.println("========================================");
        System.out.println("1. 가맹점 등록");
        System.out.println("2. 전체 가맹점 조회");
        System.out.println("3. 가맹점 상세조회 및 관리 (수정/삭제)");
        System.out.println("0. 메인 메뉴로 이동");
        System.out.println("========================================");
    }

    private static void insertMerchant() {
        try {
            printTitle("가맹점 등록");
            System.out.print("가맹점명 입력: ");
            String merchantName = sc.nextLine();
            System.out.print("업종 입력: ");
            String businessType = sc.nextLine();
            System.out.print("가맹점주소 입력: ");
            String address = sc.nextLine();
            System.out.print("전화번호 입력: ");
            String phone = sc.nextLine();

            MerchantVO merchant = merchantDao.create(new MerchantVO(merchantName, businessType, address, phone));
            System.out.println();
            System.out.println("[등록 완료] 가맹점번호: " + merchant.getMerchantNo()
                    + " | " + merchant.getMerchantName()
                    + " | 업종: " + merchant.getBusinessType()
                    + " | 전화번호: " + merchant.getPhone());
        } catch (SQLException e) {
            System.out.println("[DB 오류] 가맹점 등록 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private static void selectMerchants() {
        try {
            printTitle("전체 가맹점 조회");
            List<MerchantVO> merchants = merchantDao.getAll();

            if (merchants.isEmpty()) {
                System.out.println("등록된 가맹점이 없습니다.");
            } else {
                for (MerchantVO merchant : merchants) {
                    printMerchantLine(merchant);
                }
            }

            System.out.println();
            System.out.println("[조회 완료] 전체 가맹점 수: " + merchants.size() + "개");
        } catch (SQLException e) {
            System.out.println("[DB 오류] 전체 가맹점 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private static void manageMerchant() {
        try {
            printTitle("가맹점 상세 조회 및 관리");
            int merchantNo = readInt("관리할 가맹점번호 입력: ");
            MerchantVO merchant = merchantDao.get(merchantNo);

            if (merchant == null) {
                System.out.println("[오류] 존재하지 않는 가맹점번호입니다.");
                return;
            }

            printMerchantDetail(merchant);

            while (true) {
                printManageMenu();
                int choice = readInt("선택: ");

                switch (choice) {
                    case 1:
                        updateMerchant(merchantNo);
                        merchant = merchantDao.get(merchantNo);
                        if (merchant != null) {
                            printMerchantDetail(merchant);
                        }
                        break;
                    case 2:
                        deleteMerchant(merchant);
                        return;
                    case 0:
                        return;
                    default:
                        System.out.println("[오류] 잘못된 입력입니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("[DB 오류] 가맹점 상세 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private static void updateMerchant(int merchantNo) throws SQLException {
        MerchantVO merchant = merchantDao.get(merchantNo);
        if (merchant == null) {
            System.out.println("[오류] 존재하지 않는 가맹점번호입니다.");
            return;
        }

        printTitle("가맹점 정보 수정");
        System.out.println("선택된 가맹점번호: " + merchantNo);
        System.out.println();
        printMerchantDetail(merchant);
        printUpdateMenu();

        int choice = readInt("수정할 항목 입력: ");
        int count;

        switch (choice) {
            case 1:
                System.out.println("현재 가맹점명: " + merchant.getMerchantName());
                System.out.print("새 가맹점명 입력: ");
                String merchantName = sc.nextLine();
                count = merchantDao.updateName(merchantNo, merchantName);
                break;
            case 2:
                System.out.println("현재 업종: " + merchant.getBusinessType());
                System.out.print("새 업종 입력: ");
                String businessType = sc.nextLine();
                count = merchantDao.updateBusinessType(merchantNo, businessType);
                break;
            case 3:
                System.out.println("현재 가맹점주소: " + merchant.getAddress());
                System.out.print("새 가맹점주소 입력: ");
                String address = sc.nextLine();
                count = merchantDao.updateAddress(merchantNo, address);
                break;
            case 4:
                System.out.println("현재 전화번호: " + merchant.getPhone());
                System.out.print("새 전화번호 입력: ");
                String phone = sc.nextLine();
                count = merchantDao.updatePhone(merchantNo, phone);
                break;
            case 0:
                return;
            default:
                System.out.println("[오류] 잘못된 입력입니다.");
                return;
        }

        MerchantVO updated = merchantDao.get(merchantNo);
        if (count > 0 && updated != null) {
            System.out.println();
            System.out.println("[수정 완료] 가맹점번호: " + updated.getMerchantNo()
                    + " | 가맹점명: " + updated.getMerchantName()
                    + " | 전화번호: " + updated.getPhone());
        }
    }

    private static void deleteMerchant(MerchantVO merchant) {
        try {
            printTitle("가맹점 삭제");
            System.out.println("삭제할 가맹점번호: " + merchant.getMerchantNo());
            System.out.println();
            printMerchantDetail(merchant);
            System.out.println("정말 삭제하시겠습니까? (Y/N): ");
            String confirm = sc.nextLine().trim();

            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("삭제를 취소했습니다.");
                return;
            }

            int count = merchantDao.delete(merchant.getMerchantNo());
            if (count > 0) {
                System.out.println("[삭제 완료] 가맹점번호: " + merchant.getMerchantNo()
                        + " | " + merchant.getMerchantName()
                        + " | 전화번호: " + merchant.getPhone());
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[삭제 실패] 결제내역에서 사용 중인 가맹점은 삭제할 수 없습니다.");
        } catch (SQLException e) {
            System.out.println("[DB 오류] 가맹점 삭제 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private static void printTitle(String title) {
        System.out.println("\n========================================");
        System.out.println("          " + title);
        System.out.println("========================================");
    }

    private static void printManageMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("          관리 메뉴");
        System.out.println("========================================");
        System.out.println("1. 가맹점 정보 수정");
        System.out.println("2. 가맹점 삭제");
        System.out.println("0. 가맹점 관리 메뉴로 이동");
        System.out.println("========================================");
    }

    private static void printUpdateMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("          수정 항목 선택");
        System.out.println("========================================");
        System.out.println("1. 가맹점명 수정");
        System.out.println("2. 업종 수정");
        System.out.println("3. 가맹점주소 수정");
        System.out.println("4. 전화번호 수정");
        System.out.println("0. 가맹점 관리 메뉴로 이동");
        System.out.println("========================================");
    }

    private static void printMerchantLine(MerchantVO merchant) {
        System.out.println("가맹점번호: " + merchant.getMerchantNo()
                + " | 가맹점명: " + merchant.getMerchantName()
                + " | 업종: " + merchant.getBusinessType()
                + " | 전화번호: " + merchant.getPhone());
    }

    private static void printMerchantDetail(MerchantVO merchant) {
        System.out.println("가맹점번호: " + merchant.getMerchantNo());
        System.out.println("가맹점명: " + merchant.getMerchantName());
        System.out.println("업종: " + merchant.getBusinessType());
        System.out.println("가맹점주소: " + merchant.getAddress());
        System.out.println("전화번호: " + merchant.getPhone());
    }

    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }
}
