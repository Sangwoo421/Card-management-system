package org.example;

import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);

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
                case 1: CustomerView.customerMenu(); break;
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
}