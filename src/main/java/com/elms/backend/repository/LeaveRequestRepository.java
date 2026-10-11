package com.elms.backend.repository;

import com.elms.backend.entity.LeaveRequest;
import com.elms.backend.entity.LeaveStatus;
import com.elms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserOrderByAppliedAtDesc(User user);

    List<LeaveRequest> findByUserAndStatus(
            User user,
            LeaveStatus status
    );

    List<LeaveRequest> findByStatusOrderByAppliedAtDesc(
            LeaveStatus status
    );

    boolean existsByUserAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            User user,
            LeaveStatus status,
            LocalDate endDate,
            LocalDate startDate
    );
}