
package com.elms.backend.controller;

import com.elms.backend.dto.UserResponse;
import com.elms.backend.entity.Role;
import com.elms.backend.entity.User;
import com.elms.backend.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserResponse::new)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new UserResponse(userService.getUserById(id))
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                new UserResponse(userService.getUserByEmail(email))
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody User user) {

        User savedUser = userService.saveUser(user);

        return ResponseEntity.ok(new UserResponse(savedUser));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        return ResponseEntity.ok(
                new UserResponse(userService.updateRole(id, role))
        );
    }

    @PutMapping("/{id}/department")
    public ResponseEntity<UserResponse> assignDepartment(
            @PathVariable Long id,
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                new UserResponse(
                        userService.assignDepartment(id, departmentId)
                )
        );
    }

    @DeleteMapping("/{id}/department")
    public ResponseEntity<UserResponse> removeDepartment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new UserResponse(userService.removeDepartment(id))
        );
    }

    @PutMapping("/{id}/manager")
    public ResponseEntity<UserResponse> assignManager(
            @PathVariable Long id,
            @RequestParam Long managerId) {

        return ResponseEntity.ok(
                new UserResponse(userService.assignManager(id, managerId))
        );
    }

    @DeleteMapping("/{id}/manager")
    public ResponseEntity<UserResponse> removeManager(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new UserResponse(userService.removeManager(id))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok("User deleted successfully");
    }
}
