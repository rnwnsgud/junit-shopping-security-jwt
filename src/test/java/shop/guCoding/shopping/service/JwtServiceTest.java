package shop.guCoding.shopping.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.config.dummy.DummyObject;
import shop.guCoding.shopping.config.jwt.JwtAuthenticationFilter;
import shop.guCoding.shopping.config.jwt.JwtAuthorizationFilter;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;
import shop.guCoding.shopping.domain.user.UserRepository;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Sql("classpath:db/teardown.sql")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class JwtServiceTest extends DummyObject {
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;
    // 필터를 빈으로 등록하니깐
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;


    private static String TOKEN_PREFIX = "Bearer ";

    private static String SECRET = "구코딩";

    private String createAccessToken() {
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);

        String accessToken = jwtService.accessTokenCreate(loginUser);
        return accessToken;
    }

    private String createRefreshToken() {

        String refreshToken = jwtService.refreshTokenCreate();
        return refreshToken;
    }

    @Test
    public void createToken_test() throws Exception {
        //given

        //when
        String accessToken = createAccessToken();
        String refreshToken = createRefreshToken();

        //then
        assertTrue(accessToken.startsWith(TOKEN_PREFIX));
        assertTrue(refreshToken.startsWith(TOKEN_PREFIX));

    }

    @Test
    public void verify_test() throws Exception {
        //given
        String token = createAccessToken();
        String accessToken = token.replace(TOKEN_PREFIX, "");
        //when
        LoginUser loginUser = jwtService.accessTokenVerify(accessToken);

        //then
        assertThat(loginUser.getUser().getId()).isEqualTo(1L);
        assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
    
    @Test
    public void reissueRefreshToken_test() throws Exception {
        //given
        String expiredRefreshToken = jwtService.testRefreshTokenCreate().replace("Bearer ","");
        //when

        //then
        assertThat(jwtService.reissueRefreshToken(expiredRefreshToken)).isEqualTo(true);
    }
    
    @Test
    public void findUserWithRefreshToken_test() throws Exception {
        //given
        String refreshToken = createRefreshToken();
        User refreshUser = newUser("ssar", "쌀", refreshToken);
        userRepository.save(refreshUser);
        //when
        LoginUser loginUser = jwtService.findUserWithRefreshToken(refreshToken);
        System.out.println("테스트 : loginUser" + loginUser);

        //then
    }

}