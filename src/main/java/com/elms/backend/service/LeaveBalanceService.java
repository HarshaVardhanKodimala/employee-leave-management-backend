package com.elms.backend.service;

import com.elms.backend.entity.LeaveBalance;
import com.elms.backend.entity.LeaveType;
import com.elms.backend.entity.User;
import com.elms.backend.repository.LeaveBalanceRepository;
import com.elms.backend.repository.LeaveTypeRepository;
import com.elms.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveBalanceService(
            LeaveBalanceRepository leaveBalanceRepository,
            UserRepository userRepository,
            LeaveTypeRepository leaveTypeRepository) {

        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    public List<LeaveBalance> getAllBalances() {
        return leaveBalanceRepository.findAll();
    }

    public LeaveBalance getBalanceById(Long id) {

        return leaveBalanceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave balance not found"));
    }

    public List<LeaveBalance> getBalancesByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return leaveBalanceRepository.findByUser(user);
    }

    public List<LeaveBalance> getMyBalances(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return leaveBalanceRepository.findByUser(user);
    }

    public LeaveBalance createBalance(
            LeaveBalance leaveBalance) {

        if (leaveBalance.getUser() == null ||
                leaveBalance.getUser().getId() == null) {

            throw new RuntimeException("User is required");
        }

        if (leaveBalance.getLeaveType() == null ||
                leaveBalance.getLeaveType().getId() == null) {

            throw new RuntimeException(
                    "Leave type is required");
        }

        User user = userRepository
                .findById(leaveBalance.getUser().getId())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        LeaveType leaveType = leaveTypeRepository
                .findById(leaveBalance.getLeaveType().getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave type not found"));

        leaveBalance.setUser(user);
        leaveBalance.setLeaveType(leaveType);

        if (leaveBalance.getUsedDays() == null) {
            leaveBalance.setUsedDays(0);
        }

        return leaveBalanceRepository.save(leaveBalance);
    }

    public void deleteBalance(Long id) {

        if (!leaveBalanceRepository.existsById(id)) {

            throw new RuntimeException(
                    "Leave balance not found");
        }

        leaveBalanceRepository.deleteById(id);
    }
}