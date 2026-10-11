
package com.elms.backend.service;

import com.elms.backend.entity.Department;
import com.elms.backend.entity.LeaveBalance;
import com.elms.backend.entity.LeaveType;
import com.elms.backend.entity.Role;
import com.elms.backend.entity.User;
import com.elms.backend.repository.DepartmentRepository;
import com.elms.backend.repository.LeaveBalanceRepository;
import com.elms.backend.repository.LeaveRequestRepository;
import com.elms.backend.repository.LeaveTypeRepository;
import com.elms.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User saveUser(User user) {
        boolean isNewUser = user.getId() == null;

        if (isNewUser) {
            if (user.getFullName() == null || user.getFullName().isBlank()) {
                throw new RuntimeException("Full name is required");
            }

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new RuntimeException("Email is required");
            }

            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Email already registered");
            }

            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new RuntimeException("Password is required");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (user.getRole() == null) {
                user.setRole(Role.EMPLOYEE);
            }
        }

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.EMPLOYEE) {
            List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

            for (LeaveType leaveType : leaveTypes) {
                boolean exists = leaveBalanceRepository
                        .findByUserAndLeaveType(savedUser, leaveType)
                        .isPresent();

                if (!exists) {
                    LeaveBalance balance = new LeaveBalance(
                            savedUser,
                            leaveType,
                            leaveType.getDefaultDays()
                    );

                    leaveBalanceRepository.save(balance);
                }
            }
        }

        return savedUser;
    }

    @Transactional
    public User assignDepartment(Long userId, Long departmentId) {
        User user = getUserById(userId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new RuntimeException("Department not found"));

        user.setDepartment(department);
        return userRepository.save(user);
    }

    @Transactional
    public User removeDepartment(Long userId) {
        User user = getUserById(userId);
        user.setDepartment(null);
        return userRepository.save(user);
    }

    @Transactional
    public User assignManager(Long userId, Long managerId) {
        User user = getUserById(userId);

        if (userId.equals(managerId)) {
            throw new RuntimeException("A user cannot be their own manager");
        }

        User manager = getUserById(managerId);

        if (manager.getRole() != Role.MANAGER
                && manager.getRole() != Role.ADMIN) {
            throw new RuntimeException(
                    "Select a user with MANAGER or ADMIN role");
        }

        user.setManager(manager);
        return userRepository.save(user);
    }

    @Transactional
    public User removeManager(Long userId) {
        User user = getUserById(userId);
        user.setManager(null);
        return userRepository.save(user);
    }

    @Transactional
    public User updateRole(Long id, Role role) {
        if (role == null) {
            throw new RuntimeException("Role is required");
        }

        User user = getUserById(id);
        user.setRole(role);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        // Remove the user's leave requests.
        leaveRequestRepository.deleteAll(
                leaveRequestRepository.findByUserOrderByAppliedAtDesc(user)
        );

        // Remove the user's leave balances.
        leaveBalanceRepository.deleteAll(
                leaveBalanceRepository.findByUser(user)
        );

        // Remove manager references from direct reports.
        List<User> directReports = userRepository.findAll()
                .stream()
                .filter(other -> other.getManager() != null
                        && other.getManager().getId().equals(id))
                .toList();

        for (User report : directReports) {
            report.setManager(null);
        }

        userRepository.saveAll(directReports);

        // Delete the user account.
        userRepository.delete(user);
    }
}
