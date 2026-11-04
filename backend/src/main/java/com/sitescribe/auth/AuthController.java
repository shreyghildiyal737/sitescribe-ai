package com.sitescribe.auth;

import com.sitescribe.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/bootstrap")
    public ApiResponse<AuthResponse> bootstrap(@Valid @RequestBody BootstrapRequest req) {
        return ApiResponse.ok(auth.bootstrap(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(auth.login(req));
    }
}
