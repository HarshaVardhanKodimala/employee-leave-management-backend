
package com.elms.backend.controller;

import com.elms.backend.entity.LeaveRequest;
import com.elms.backend.service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "http://localhost:5173")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isReviewer(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllRequests(
            Authentication authentication) {

        if (!isReviewer(authentication)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers and administrators can view review requests");
        }

        return ResponseEntity.ok(
                leaveRequestService.getRequestsForReviewer(
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequest>> getMyRequests(
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveRequestService.getMyRequests(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getRequestById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveRequestService.getRequestById(
                        id,
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveRequest>> getRequestsByUser(
            @PathVariable Long userId,
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveRequestService.getRequestsByUser(
                        userId,
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> createRequest(
            @RequestBody LeaveRequest leaveRequest,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                leaveRequestService.createRequest(
                        leaveRequest,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {

        String remarks = body == null ? "" : body.get("remarks");

        return ResponseEntity.ok(
                leaveRequestService.approveRequest(
                        id,
                        remarks,
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {

        String remarks = body == null ? "" : body.get("remarks");

        return ResponseEntity.ok(
                leaveRequestService.rejectRequest(
                        id,
                        remarks,
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRequest(
            @PathVariable Long id,
            Authentication authentication) {

        leaveRequestService.deleteRequest(
                id,
                authentication.getName(),
                isAdmin(authentication)
        );

        return ResponseEntity.ok("Leave request deleted successfully");
    }
}
