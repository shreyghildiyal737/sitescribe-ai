package com.sitescribe.auth;

import com.sitescribe.common.NotFoundException;
import com.sitescribe.company.Company;
import com.sitescribe.company.CompanyRepository;
import com.sitescribe.user.AppUser;
import com.sitescribe.user.UserRepository;
import com.sitescribe.user.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CompanyRepository companies;
    private final UserRepository users;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder;

    public AuthService(CompanyRepository companies, UserRepository users,
                       JwtService jwtService, BCryptPasswordEncoder encoder) {
        this.companies = companies;
        this.users = users;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    @Transactional
    public AuthResponse bootstrap(BootstrapRequest req) {
        Company company = companies.save(new Company(req.companyName(), req.preferredLanguage()));
        AppUser user = users.save(new AppUser(
            company, req.name(), req.email(),
            UserRole.OWNER, req.preferredLanguage(),
            encoder.encode(req.password())
        ));
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = users.findByEmail(req.email())
            .orElseThrow(() -> new NotFoundException("No account found with that email."));

        if (user.getPasswordHash() == null || !encoder.matches(req.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("Incorrect password.");

        return toResponse(user);
    }

    private AuthResponse toResponse(AppUser user) {
        return new AuthResponse(
            user.getId(),
            user.getCompany().getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            jwtService.generate(user)
        );
    }
}
