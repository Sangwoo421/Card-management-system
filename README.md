# 💳 카드 결제 관리 시스템

KB IT's Your Life 7기 DB 모델링 + JDBC 프로젝트

---

## 👥 팀원

| 이름 | 담당 |
|------|------|
| 안상우 | 팀장 / 고객 관리 |
| 정성윤 | 카드 관리 / 카드상품 관리 |
| 고현준 | 결제 처리 |
| 조진혁 | 가맹점 관리 |

---

## 📋 프로젝트 개요

카드사의 카드 결제 관리 업무를 콘솔 기반으로 구현한 시스템입니다.  
고객 관리, 카드 발급, 결제 처리, 가맹점 관리 기능을 제공합니다.

---

## 🗂️ ERD

| 테이블 | 설명 |
|--------|------|
| 고객 | 카드 발급 고객 정보 |
| 카드 | 고객별 발급 카드 정보 |
| 카드상품 | 카드 상품 목록 |
| 가맹점 | 결제 가맹점 정보 |
| 결제내역 | 카드 결제 및 취소 내역 |

---

## ⚙️ 기술 스택

- Java 22
- MySQL 8.x
- JDBC (mysql-connector-j 8.3.0)
- Gradle
- IntelliJ IDEA

---

## 🚀 실행 방법

### 1. 레포지토리 클론
```bash
git clone https://github.com/Sangwoo421/Card-management-system.git
```

### 2. DB 세팅
MySQL Workbench에서 `ddl.sql` 실행

### 3. application.properties 수정
```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://127.0.0.1:3306/card_management_system
id=root
password=본인MySQL비밀번호
```

### 4. IntelliJ에서 실행
`src/main/java/org/example/Main.java` 실행

---

## 📌 주요 기능

### 1. 고객 관리
- 고객 등록
- 고객 조회 (이름 / 전화번호 / 가입일 / 카드번호로 검색)
- 고객 정보 수정
- 고객 삭제

### 2. 카드 관리
- 고객 검색 및 보유 카드 조회
- 카드 발급
- 카드 상태 변경 (정상 / 정지 / 해지)
- 카드 한도 변경

### 3. 결제 처리
- 결제
- 결제 취소
- 카드별 결제내역 조회

### 4. 가맹점 관리
- 가맹점 등록
- 전체 가맹점 조회
- 가맹점 정보 수정
- 가맹점 삭제

### 5. 카드상품 관리
- 카드상품 등록
- 전체 카드상품 조회
- 카드상품 수정
- 카드상품 삭제