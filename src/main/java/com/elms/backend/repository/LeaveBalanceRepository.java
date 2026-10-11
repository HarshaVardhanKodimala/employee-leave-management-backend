package com.elms.backend.repository;

import com.elms.backend.entity.LeaveBalance;
import com.elms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByUser(User user);

    Optional<LeaveBalance> findByUserAndLeaveType(
            User user,
            com.elms.backend.entity.LeaveType leaveType
    );
}