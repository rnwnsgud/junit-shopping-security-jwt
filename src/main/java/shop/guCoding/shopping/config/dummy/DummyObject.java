package shop.guCoding.shopping.config.dummy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;

import java.time.LocalDateTime;

public class DummyObject {

    protected User newMockUser(Long id, String username, String fullname) {

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");

        return User.builder()
                .id(id)
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}