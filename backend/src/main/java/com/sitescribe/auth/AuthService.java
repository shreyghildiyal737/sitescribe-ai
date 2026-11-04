package com.sitescribe.auth;

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

    static final String INVALID_CREDENTIALS = "Invalid email or password.";
    private static final String DUMMY_HASH = new BCryptPasswordEncoder().encode("timing-equaliser");

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
        AppUser user = users.findByEmail(req.email()).orElse(null);

        // One message for both failures, and a bcrypt check even when the email is unknown, so
        // neither the wording nor the response time reveals which emails have accounts.
        String hash = (user == null || user.getPasswordHash() == null) ? DUMMY_HASH : user.getPasswordHash();
        boolean passwordMatches = encoder.matches(req.password(), hash);
        if (user == null || user.getPasswordHash() == null || !passwordMatches)
            throw new IllegalArgumentException(INVALID_CREDENTIALS);

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
