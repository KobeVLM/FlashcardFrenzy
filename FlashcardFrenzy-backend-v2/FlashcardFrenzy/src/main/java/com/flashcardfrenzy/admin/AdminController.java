package com.flashcardfrenzy.admin;

import com.flashcardfrenzy.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MVP — View (Controller)
 * HTTP routing and response wrapping only. All logic in AdminPresenter.
 * ROLE_ADMIN is enforced by SecurityConfig — no @PreAuthorize needed.
 *
 * Routes:
 *   GET    /api/v1/admin/stats        — admin only
 *   GET    /api/v1/admin/users        — admin only
 *   DELETE /api/v1/admin/users/{id}   — admin only
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminPresenter adminPresenter;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDto.StatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminPresenter.getStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminDto.AdminUserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminPresenter.getAllUsers()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        adminPresenter.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted successfully"));
    }
}
