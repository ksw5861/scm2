package com.yedam.scm.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.EmployeeSimpleVO;
import com.yedam.scm.vo.EmployeeVO;

@Mapper
public interface EmployeeMapper {

  List<EmployeeSimpleVO> selectEmployeeSimpleListByCondition(
    @Param("condition") EmployeeSearchDTO condition,
    @Param("paging") PageDTO paging
  );

  long selectEmployeeCountByCondition(
    @Param("condition") EmployeeSearchDTO condition
  );

  EmployeeVO selectEmployeeById(
    @Param("empId") String empId
  );

  int insertEmployee(EmployeeVO emp);

  int updateEmployeeById(EmployeeVO emp);

  int deleteEmployeeById(
    @Param("empId") String empId
  );

}
