package shop.guCoding.shopping.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.service.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${jwt.access_header:null}")
    private String ACCESS_HEADER;

    @Value("${jwt.refresh_header:null}")
    private String REFRESH_HEADER;

    @Value("${jwt.secret}")
    private String SECRET;

    private final JwtService jwtService;




    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(authenticationManager);
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (jwtService.checkHeaderVerify(request)) {
//            log.debug("principal" + request.getUserPrincipal().getName()); null


            String accessToken = request.getHeader(ACCESS_HEADER).replace("Bearer ",""); // Bearer 앞에 없앤 순수한 토큰
            String refreshToken = request.getHeader(REFRESH_HEADER).replace("Bearer ","");

            // accessToken 검증
            try {
                LoginUser loginUser = jwtService.accessTokenVerify(accessToken);
                Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
//                jwtService.refreshTokenVerify(refreshToken); // 이 메서드 자체가 이상 -> accessToken을 넣으니 괜찮음 -> refreshToken을 만들때 이상해짐 -> 유효기간이 3~4주가 최대였음..

//                log.debug("securityContext" + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            } catch (TokenExpiredException e) {
                log.error("accessToken 오류");
                // refreshToken 검증
                jwtService.refreshTokenVerify(refreshToken);
                log.error("accessToken 오류2");
                LoginUser loginUser = jwtService.findUserWithRefreshToken(refreshToken);
                String reissuedAccessToken = jwtService.accessTokenCreate(loginUser);
                log.debug("재발급 accessToken " + reissuedAccessToken);
                response.setHeader(ACCESS_HEADER, reissuedAccessToken);

            }

            // refreshToken 이 7일 이내 만료 될 경우 refreshToken 재발급
            if (jwtService.reissueRefreshToken(refreshToken)) {

                refreshToken = jwtService.refreshTokenCreate();
                response.setHeader(REFRESH_HEADER, refreshToken);

            }
        }


        chain.doFilter(request, response);
    }
}
