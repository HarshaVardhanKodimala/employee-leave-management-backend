package com.elms.backend.controller;

import com.elms.backend.entity.LeaveBalance;
import com.elms.backend.service.LeaveBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-balances")
@CrossOrigin(origins = "http://localhost:5173")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(
            LeaveBalanceService leaveBalanceService) {

        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveBalance>> getAllBalances() {

        return ResponseEntity.ok(
                leaveBalanceService.getAllBalances()
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveBalance>> getMyBalances(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                leaveBalanceService.getMyBalances(email)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveBalance> getBalanceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leaveBalanceService.getBalanceById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveBalance>> getBalancesByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                leaveBalanceService.getBalancesByUser(userId)
        );
    }

    @PostMapping
    public ResponseEntity<LeaveBalance> createBalance(
            @RequestBody LeaveBalance leaveBalance) {

        return ResponseEntity.ok(
                leaveBalanceService.createBalance(
                        leaveBalance)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBalance(
            @PathVariable Long id) {

        leaveBalanceService.deleteBalance(id);

        return ResponseEntity.ok(
                "Leave balance deleted successfully"
        );
    }
}