package com.elms.backend.controller;

import com.elms.backend.entity.LeaveType;
import com.elms.backend.service.LeaveTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping
    public ResponseEntity<LeaveType> createLeaveType(
            @RequestBody LeaveType leaveType) {

        return ResponseEntity.ok(
                leaveTypeService.createLeaveType(leaveType)
        );
    }

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {

        return ResponseEntity.ok(
                leaveTypeService.getAllLeaveTypes()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveType> getLeaveTypeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leaveTypeService.getLeaveTypeById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLeaveType(
            @PathVariable Long id) {

        leaveTypeService.deleteLeaveType(id);

        return ResponseEntity.ok("Leave type deleted successfully");
    }
}