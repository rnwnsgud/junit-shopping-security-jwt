package shop.guCoding.shopping.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;

import java.util.Date;


public class JwtProcess {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${jwt.expiration_time:null}")
    private static Integer EXPIRATION_TIME;

    @Value("${jwt.secret:null}")
    private static String SECRET;

    @Value("${jwt.token_prefix:null}")
    private static String TOKEN_PREFIX;

    // access 토큰 생성
    public static String accessTokenCreate(LoginUser loginUser) {
        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // refresh 토큰
    public static String refreshTokenCreate(LoginUser loginUser) {
        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 4))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // 토큰 검증 (return 되는 LoginUser 객체를 강제로 시큐리티 세션에 직접 주입) ==> 왜그랬더라?? 찾아보자
    public static LoginUser verify(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token);
        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
        User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();
        LoginUser loginUser = new LoginUser(user);
        return loginUser;
    }
}
