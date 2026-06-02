package org.example;

import org.example.dao.CustomerDao;
import org.example.dao.CustomerDaoImpl;
import org.example.domain.CustomerVO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static CustomerDao customerDao = new CustomerDaoImpl();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("       카드 결제 관리 시스템");
            System.out.println("========================================");
            System.out.println("1. 고객 관리");
            System.out.println("2. 카드 관리");
            System.out.println("3. 결제 처리");
            System.out.println("4. 가맹점 관리");
            System.out.println("5. 카드상품 관리");
            System.out.println("0. 종료");
            System.out.println("========================================");
            System.out.print("선택: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: customerMenu(); break;
                case 2: System.out.println("카드 관리 - 준비 중"); break;
                case 3: System.out.println("결제 처리 - 준비 중"); break;
                case 4: System.out.println("가맹점 관리 - 준비 중"); break;
                case 5: System.out.println("카드상품 관리 - 준비 중"); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // 고객 관리 서브메뉴
    static void customerMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("         고객 관리");
            System.out.println("========================================");
            System.out.println("1. 고객 등록");
            System.out.println("2. 고객 조회");
            System.out.println("3. 고객 정보 수정");
            System.out.println("4. 고객 삭제");
            System.out.println("0. 메인 메뉴로 이동");
            System.out.println("========================================");
            System.out.print("선택: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: insertCustomer(); break;
                case 2: selectCustomer(); break;
                case 3: updateCustomer(); break;
                case 4: deleteCustomer(); break;
                case 0: return;
                default: System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // 고객 등록
    static void insertCustomer() {
        try {
            System.out.println("\n========================================");
            System.out.println("         고객 등록");
            System.out.println("========================================");
            System.out.print("이름 입력: ");
            String name = sc.nextLine();
            System.out.print("주민등록번호 입력: ");
            String ssn = sc.nextLine();
            System.out.print("전화번호 입력: ");
            String phone = sc.nextLine();
            System.out.print("주소 입력: ");
            String address = sc.nextLine();

            CustomerVO customer = new CustomerVO(name, ssn, phone, address);
            int result = customerDao.create(customer);
            if (result > 0) {
                System.out.println("[등록 완료] " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 고객 조회
    static void selectCustomer() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("         고객 조회");
            System.out.println("========================================");
            System.out.println("1. 이름으로 검색");
            System.out.println("2. 전화번호로 검색");
            System.out.println("3. 가입일로 검색");
            System.out.println("4. 카드번호로 검색");
            System.out.println("0. 이전 메뉴");
            System.out.println("========================================");
            System.out.print("선택: ");

            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 0) return;

            try {
                List<CustomerVO> list = null;
                CustomerVO single = null;

                switch (choice) {
                    case 1:
                        System.out.print("이름 입력: ");
                        list = customerDao.getByName(sc.nextLine());
                        break;
                    case 2:
                        System.out.print("전화번호 입력: ");
                        list = customerDao.getByPhone(sc.nextLine());
                        break;
                    case 3:
                        System.out.print("가입일 입력 (예: 2026-05-26): ");
                        list = customerDao.getByJoinDate(sc.nextLine());
                        break;
                    case 4:
                        System.out.print("카드번호 입력: ");
                        single = customerDao.getByCardNo(sc.nextInt());
                        sc.nextLine();
                        break;
                    default:
                        System.out.println("잘못된 입력입니다.");
                        continue;
                }

                System.out.println("\n----------------------------------------");
                if (single != null) {
                    printCustomer(single);
                } else if (list != null && !list.isEmpty()) {
                    for (CustomerVO c : list) printCustomer(c);
                    System.out.println("총 " + list.size() + "명");
                } else {
                    System.out.println("검색 결과가 없습니다.");
                }
                System.out.println("----------------------------------------");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 고객 정보 출력
    static void printCustomer(CustomerVO c) {
        try {
            String cards = ((CustomerDaoImpl) customerDao).getCardNumbers(c.getCustomerNo());
            System.out.println("고객번호: " + c.getCustomerNo());
            System.out.println("이름: " + c.getName());
            System.out.println("주민등록번호: " + c.getSsn());
            System.out.println("전화번호: " + c.getPhone());
            System.out.println("주소: " + c.getAddress());
            System.out.println("가입일: " + c.getJoinDate());
            System.out.println("보유 카드번호: " + cards);
            System.out.println("----------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 고객 정보 수정
    static void updateCustomer() {
        try {
            System.out.println("\n========================================");
            System.out.println("         고객 정보 수정");
            System.out.println("========================================");
            System.out.print("수정할 고객번호 입력: ");
            int customerNo = sc.nextInt();
            sc.nextLine();

            CustomerVO current = customerDao.get(customerNo);
            if (current == null) {
                System.out.println("[오류] 존재하지 않는 고객번호입니다.");
                return;
            }
            System.out.println("\n[현재 정보]");
            System.out.println("이름: " + current.getName() + " | 전화번호: " + current.getPhone() + " | 주소: " + current.getAddress());

            System.out.print("새 전화번호 입력 (변경 없으면 엔터): ");
            String phone = sc.nextLine();
            System.out.print("새 주소 입력 (변경 없으면 엔터): ");
            String address = sc.nextLine();

            if (phone.isEmpty()) phone = current.getPhone();
            if (address.isEmpty()) address = current.getAddress();

            CustomerVO customer = new CustomerVO();
            customer.setCustomerNo(customerNo);
            customer.setPhone(phone);
            customer.setAddress(address);

            int result = customerDao.update(customer);
            if (result > 0) {
                System.out.println("[수정 완료] 고객번호: " + customerNo + " | " + current.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 고객 삭제
    static void deleteCustomer() {
        try {
            System.out.println("\n========================================");
            System.out.println("         고객 삭제");
            System.out.println("========================================");
            System.out.print("삭제할 고객번호 입력: ");
            int customerNo = sc.nextInt();
            sc.nextLine();

            CustomerVO customer = customerDao.get(customerNo);
            if (customer == null) {
                System.out.println("[오류] 존재하지 않는 고객번호입니다.");
                return;
            }

            System.out.println("[경고] 고객번호 " + customerNo + " | " + customer.getName() + " 을 삭제합니다.");
            System.out.print("정말 삭제하시겠습니까? (Y/N): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("Y")) {
                int result = customerDao.delete(customerNo);
                if (result > 0) {
                    System.out.println("[삭제 완료] 고객번호: " + customerNo + " | " + customer.getName());
                }
            } else {
                System.out.println("삭제를 취소했습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}