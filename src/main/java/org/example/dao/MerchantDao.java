package org.example.dao;

import org.example.domain.MerchantVO;

import java.sql.SQLException;
import java.util.List;

public interface MerchantDao {
    // 가맹점 등록
    MerchantVO create(MerchantVO merchant) throws SQLException;

    // 전체 가맹점 조회
    List<MerchantVO> getAll() throws SQLException;

    // 가맹점 단건 조회
    MerchantVO get(int merchantNo) throws SQLException;

    // 가맹점명 수정
    int updateName(int merchantNo, String merchantName) throws SQLException;

    // 업종 수정
    int updateBusinessType(int merchantNo, String businessType) throws SQLException;

    // 가맹점주소 수정
    int updateAddress(int merchantNo, String address) throws SQLException;

    // 전화번호 수정
    int updatePhone(int merchantNo, String phone) throws SQLException;

    // 가맹점 삭제
    int delete(int merchantNo) throws SQLException;
}
