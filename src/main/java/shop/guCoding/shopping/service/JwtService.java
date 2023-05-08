package shop.guCoding.shopping.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;
import shop.guCoding.shopping.domain.user.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class JwtService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.token_prefix:null}")
    private String TOKEN_PREFIX;

    @Value("${jwt.expiration_time:null}")
    private Integer EXPIRATION_TIME;

    @Value("${jwt.access_header:null}")
    private String ACCESS_HEADER;

    @Value("${jwt.access_header:null}")
    private String REFRESH_HEADER;

    public void saveRefreshToken(LoginUser loginUser, String token) {
        log.debug("saveRefreshToken");
        Optional<User> userOP = userRepository.findByEmail(loginUser.getEmail());
        if (userOP.isPresent()) {
            log.debug("유저가 있네요 리프레쉬 토큰을 저장합니다.");
            User user = userOP.get();
            user.saveRefreshToken(token);
        }
    }

    // accessToken 생성
    public String accessTokenCreate(LoginUser loginUser) {

        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7 )) // 1주일
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // refreshToken 생성
    public String refreshTokenCreate() {
        Date expirationDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7 * 4); // 4주일
        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // 유효기간 얼마안남은 테스트용

    public String testAccessTokenCreate(LoginUser loginUser) {

        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + 100))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }
    public String testRefreshTokenCreate() {
        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME - 100)) // 1주일 이내
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // header 검증
    public boolean checkHeaderVerify(HttpServletRequest request) {
//        log.debug("디버그 : access token 헤더 있니?" + ACCESS_HEADER);
        String accessHeader = request.getHeader(ACCESS_HEADER);
        String refreshHeader = request.getHeader(REFRESH_HEADER);

        if (accessHeader != null || refreshHeader != null) {
            return true;
        }

        if (accessHeader == null) {
            log.error("AccessToken 헤더가 없어요");
        }
        if (refreshHeader == null) {
            log.error("RefreshToken 헤더가 없어요");
        }

        return false;
    }

    // accessToken 검증 및 세션생성
    public LoginUser accessTokenVerify(String token) {
//        log.debug("accessToken 검증할게요 " + token);
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token);

        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
//        log.debug("토큰의 유저의 role " + role);
        User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();
        LoginUser loginUser = new LoginUser(user);
        return loginUser;
    }


    // refreshToken 검증
    public void refreshTokenVerify(String token) {
       JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token);
    }


   // refresh 토큰 1주일 남으면 재발급
   public boolean reissueRefreshToken(String token) {
       try {
           Date expiresAt = JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token).getExpiresAt();
//           log.debug("만료일 " + expiresAt);
           Date current = new Date(System.currentTimeMillis());
           Calendar calendar = Calendar.getInstance(); // Calendar 가 추가되면서 대부분의 Date 의 메서드가 deprecated
           calendar.setTime(current);
           calendar.add(Calendar.DATE, 7); // 날짜 7일 증가

           Date sevenDaysFromNow = calendar.getTime();

           // 7일 이내에 만료
           if (expiresAt.before(sevenDaysFromNow)) {
               log.info("refreshToken 7일 이내 만료 재발급 필요");
               return true;
           }
       } catch (TokenExpiredException e) {
           return true;
       }

    return false;

    }

    // refresh 토큰으로 유저찾고 LoginUser 리턴
    public LoginUser findUserWithRefreshToken(String token) {
        Optional<User> userOP = userRepository.findByRefreshToken(token);
        if (userOP.isPresent()) {
            User user = userOP.get();
            LoginUser loginUser = new LoginUser(user);
            return loginUser;
        }

        return null;
    }



}
