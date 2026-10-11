
package com.elms.backend.service;

import com.elms.backend.entity.LeaveBalance;
import com.elms.backend.entity.LeaveRequest;
import com.elms.backend.entity.LeaveStatus;
import com.elms.backend.entity.LeaveType;
import com.elms.backend.entity.Role;
import com.elms.backend.entity.User;
import com.elms.backend.repository.LeaveBalanceRepository;
import com.elms.backend.repository.LeaveRequestRepository;
import com.elms.backend.repository.LeaveTypeRepository;
import com.elms.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveTypeRepository leaveTypeRepository) {

        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private LeaveRequest findRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave request not found"));
    }

    private boolean manages(User manager, User employee) {
        return employee.getManager() != null
                && employee.getManager().getId().equals(manager.getId());
    }

    private void requireOwnerOrAdmin(
            LeaveRequest request, User currentUser, boolean admin) {

        if (!admin && !request.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another employee's leave request");
        }
    }

    private void requireReviewer(
            LeaveRequest request, User currentUser, boolean admin) {

        if (admin) {
            return;
        }

        if (currentUser.getRole() != Role.MANAGER
                || !manages(currentUser, request.getUser())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not the assigned manager for this employee");
        }
    }

    public List<LeaveRequest> getRequestsForReviewer(
            String email, boolean admin) {

        User currentUser = getAuthenticatedUser(email);

        if (admin) {
            return leaveRequestRepository.findAll();
        }

        if (currentUser.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Manager access required");
        }

        return leaveRequestRepository.findAll().stream()
                .filter(request -> manages(currentUser, request.getUser()))
                .toList();
    }

    public LeaveRequest getRequestById(
            Long id, String email, boolean admin) {

        User currentUser = getAuthenticatedUser(email);
        LeaveRequest request = findRequest(id);

        if (admin || request.getUser().getId().equals(currentUser.getId())) {
            return request;
        }

        requireReviewer(request, currentUser, false);
        return request;
    }

    public List<LeaveRequest> getRequestsByUser(
            Long userId, String email, boolean admin) {

        User currentUser = getAuthenticatedUser(email);

        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!admin
                && !employee.getId().equals(currentUser.getId())
                && !manages(currentUser, employee)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view this employee's leave requests");
        }

        return leaveRequestRepository.findByUserOrderByAppliedAtDesc(employee);
    }

    public List<LeaveRequest> getMyRequests(String email) {
        User user = getAuthenticatedUser(email);

        return leaveRequestRepository.findByUserOrderByAppliedAtDesc(user);
    }

    public LeaveRequest createRequest(
            LeaveRequest leaveRequest, String email) {

        User user = getAuthenticatedUser(email);

        if (user.getRole() != Role.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only employees can submit leave requests");
        }

        if (leaveRequest.getLeaveType() == null
                || leaveRequest.getLeaveType().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Leave type is required");
        }

        if (leaveRequest.getStartDate() == null
                || leaveRequest.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Start date and end date are required");
        }

        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date");
        }

        LeaveType leaveType = leaveTypeRepository
                .findById(leaveRequest.getLeaveType().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave type not found"));

        long days = ChronoUnit.DAYS.between(
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        ) + 1;

        leaveRequest.setUser(user);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setNumberOfDays((int) days);
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setManagerRemarks(null);
        leaveRequest.setReviewedAt(null);

        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest approveRequest(
            Long id, String remarks, String email, boolean admin) {

        User currentUser = getAuthenticatedUser(email);
        LeaveRequest request = findRequest(id);

        requireReviewer(request, currentUser, admin);

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending requests can be approved");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByUserAndLeaveType(
                        request.getUser(),
                        request.getLeaveType())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave balance not found"));

        int remainingDays = balance.getRemainingDays();

        if (remainingDays < request.getNumberOfDays()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient leave balance. Remaining days: "
                            + remainingDays);
        }

        balance.setUsedDays(
                balance.getUsedDays() + request.getNumberOfDays());

        leaveBalanceRepository.save(balance);

        request.setStatus(LeaveStatus.APPROVED);
        request.setManagerRemarks(remarks);
        request.setReviewedAt(LocalDateTime.now());

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest rejectRequest(
            Long id, String remarks, String email, boolean admin) {

        User currentUser = getAuthenticatedUser(email);
        LeaveRequest request = findRequest(id);

        requireReviewer(request, currentUser, admin);

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending requests can be rejected");
        }

        request.setStatus(LeaveStatus.REJECTED);
        request.setManagerRemarks(remarks);
        request.setReviewedAt(LocalDateTime.now());

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public void deleteRequest(Long id, String email, boolean admin) {
        User currentUser = getAuthenticatedUser(email);
        LeaveRequest request = findRequest(id);

        if (!admin) {
            requireOwnerOrAdmin(request, currentUser, false);

            if (request.getStatus() != LeaveStatus.PENDING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Only pending requests can be deleted");
            }
        }

        leaveRequestRepository.delete(request);
    }
}
