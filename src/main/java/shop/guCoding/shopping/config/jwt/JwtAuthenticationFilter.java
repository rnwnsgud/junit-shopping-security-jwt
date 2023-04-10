package shop.guCoding.shopping.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.dto.user.UserReqDto;
import shop.guCoding.shopping.dto.user.UserRespDto;
import shop.guCoding.shopping.util.CustomResponseUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static shop.guCoding.shopping.config.jwt.JwtVO.ACCESS_HEADER;
import static shop.guCoding.shopping.config.jwt.JwtVO.REFRESH_HEADER;
import static shop.guCoding.shopping.dto.user.UserReqDto.*;
import static shop.guCoding.shopping.dto.user.UserRespDto.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {


    private final Logger log = LoggerFactory.getLogger(getClass());

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager = authenticationManager;
    }

    // Post : /login
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.debug("디버그 : attemptAuthentication 호출됨");
        try {
            // request 의 json 데이터 꺼내기
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            // 강제 로그인
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginReqDto.getEmail(), loginReqDto.getPassword()
            );
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("디버그 : successfulAuthentication 호출됨");
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        log.debug("loginUser id", loginUser.getUser().getId());
        String jwtAccessToken = JwtProcess.accessTokenCreate(loginUser);
        log.debug("jwtAccessToken", jwtAccessToken);
        String jwtRefreshToken = JwtProcess.refreshTokenCreate(loginUser);
        response.addHeader(ACCESS_HEADER, jwtAccessToken);
        response.addHeader(REFRESH_HEADER, jwtRefreshToken);

        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        log.debug("디버그 : loginRespDto", loginRespDto.getEmail());
        log.debug("디버그 : loginRespDto", loginRespDto.getId());
        log.debug("디버그 : loginRespDto", loginRespDto.getUsername());
        log.debug("디버그 : loginRespDto", loginRespDto.getCreatedAt());
        CustomResponseUtil.success(response, loginRespDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.fail(response, "로그인실패", HttpStatus.UNAUTHORIZED);
    }
}
