package com.github.farzam_raoufi.template.service.impl;

import com.github.farzam_raoufi.template.dto.auth.*;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.AuthProvider;
import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.UserRepository;
import com.github.farzam_raoufi.template.security.jwt.JwtService;
import com.github.farzam_raoufi.template.security.jwt.RefreshTokenService;
import com.github.farzam_raoufi.template.security.oauth2.SocialAuthProviderFactory;
import com.github.farzam_raoufi.template.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final SocialAuthProviderFactory socialAuthProviderFactory;

    @Override
    @Transactional
    public AuthDTO register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ErrorMessage.USERNAME_ALREADY_EXISTS, registerDTO.getUsername());
        }

        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createToken(user);

        return AuthDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthDTO login(LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_NOT_FOUND, loginDTO.getUsername()));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createToken(user);

        return AuthDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }


    @Override
    @Transactional
    public AuthDTO loginWithSocial(AuthProvider provider, String token) {
        SocialUserInfo socialUserInfo = socialAuthProviderFactory.getProvider(provider).verify(token);

        User user = userRepository.findByProviderAndProviderId(provider, socialUserInfo.getProviderId())
                .orElseGet(() -> linkOrCreateSocialUser(socialUserInfo));

        return issueTokens(user);
    }

    @Override
    public AuthDTO refreshToken(String refreshToken) {
        RefreshTokenDTO result = refreshTokenService.rotate(refreshToken);
        User user = result.user();

        String accessToken = jwtService.generateToken(user);

        return AuthDTO.builder()
                .accessToken(accessToken)
                .refreshToken(result.rawToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }


    private User linkOrCreateSocialUser(SocialUserInfo socialUserInfo) {
        return userRepository.findByEmail(socialUserInfo.getEmail())
                .map(existing -> linkSocialAccount(existing, socialUserInfo))
                .orElseGet(() -> createSocialUser(socialUserInfo));
    }

    private User linkSocialAccount(User user, SocialUserInfo socialUserInfo) {
        user.setProvider(socialUserInfo.getProvider());
        user.setProviderId(socialUserInfo.getProviderId());
        return userRepository.save(user);
    }

    private User createSocialUser(SocialUserInfo socialUserInfo) {
        User user = User.builder()
                .username(socialUserInfo.getEmail())
                .email(socialUserInfo.getEmail())
                .provider(socialUserInfo.getProvider())
                .providerId(socialUserInfo.getProviderId())
                .role(Role.USER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    private AuthDTO issueTokens(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createToken(user);
        return buildAuthDTO(user, accessToken, refreshToken);
    }

    private AuthDTO buildAuthDTO(User user, String accessToken, String refreshToken) {
        return AuthDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

}
