package shop.guCoding.shopping.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import shop.guCoding.shopping.config.dummy.DummyObject;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserRepository;
import shop.guCoding.shopping.dto.user.UserReqDto;
import shop.guCoding.shopping.dto.user.UserRespDto;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static shop.guCoding.shopping.dto.user.UserReqDto.*;
import static shop.guCoding.shopping.dto.user.UserRespDto.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends DummyObject {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    public void 회원가입_test() throws Exception {
        //given
        JoinReqDto joinReqDto =new JoinReqDto();
        joinReqDto.setUsername("ssar");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("ssar@nate.com");
        joinReqDto.setFullname("쌀");

        //stub
        when(userRepository.findByEmail(joinReqDto.getEmail())).thenReturn(Optional.empty());

        User ssar = newMockUser(1L, "ssar", "쌀");
        when(userRepository.save(any())).thenReturn(ssar);

        //when
        JoinRespDto joinRespDto = userService.회원가입(joinReqDto);
        System.out.println("테스트 : " + joinRespDto.getId());

        //then
        assertThat(joinRespDto.getUsername()).isEqualTo("ssar");

    }
}