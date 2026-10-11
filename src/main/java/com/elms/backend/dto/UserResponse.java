
package com.elms.backend.dto;

import com.elms.backend.entity.User;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerName;

    public UserResponse(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole() == null
                ? null : user.getRole().name();

        if (user.getDepartment() != null) {
            this.departmentId = user.getDepartment().getId();
            this.departmentName = user.getDepartment().getName();
        }

        if (user.getManager() != null) {
            this.managerId = user.getManager().getId();
            this.managerName = user.getManager().getFullName();
        }
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }
}
