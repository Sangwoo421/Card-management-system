package org.example.dao;

import org.example.domain.CustomerVO;
import java.sql.SQLException;
import java.util.List;

public interface CustomerDao {
    // 고객 등록
    int create(CustomerVO customer) throws SQLException;

    // 이름으로 검색
    List<CustomerVO> getByName(String name) throws SQLException;

    // 전화번호로 검색
    List<CustomerVO> getByPhone(String phone) throws SQLException;

    // 가입일로 검색
    List<CustomerVO> getByJoinDate(String joinDate) throws SQLException;

    // 카드번호로 검색
    CustomerVO getByCardNo(int cardNo) throws SQLException;

    // 고객 수정
    int update(CustomerVO customer) throws SQLException;

    // 고객 삭제
    int delete(int customerNo) throws SQLException;

    // 고객 단건 조회
    CustomerVO get(int customerNo) throws SQLException;
}