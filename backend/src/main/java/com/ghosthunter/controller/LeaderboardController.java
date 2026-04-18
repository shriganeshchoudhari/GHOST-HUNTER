package com.ghosthunter.controller;

import com.ghosthunter.dto.UserResponse;
import com.ghosthunter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for leaderboard operations.
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final UserService userService;

    /**
     * Get top users for leaderboard.
     *
     * @param limit Number of users to return (default 10)
     * @return List of top users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Leaderboard request received with limit: {}", limit);
        List<UserResponse> leaderboard = userService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
}
