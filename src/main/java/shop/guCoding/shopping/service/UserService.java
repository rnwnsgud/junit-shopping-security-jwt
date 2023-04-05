package shop.guCoding.shopping.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserRepository;
import shop.guCoding.shopping.dto.user.UserRespDto;
import shop.guCoding.shopping.handler.ex.CustomApiException;


import java.util.Optional;

import static shop.guCoding.shopping.dto.user.UserReqDto.*;
import static shop.guCoding.shopping.dto.user.UserRespDto.*;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinRespDto 회원가입(JoinReqDto joinReqDto) {
        Optional<User> userOP = userRepository.findByEmail(joinReqDto.getEmail());
        if (userOP.isPresent()) {
            throw new CustomApiException("동일한 이메일이 존재합니다.");
        }

        User userPS = userRepository.save(joinReqDto.toEntity(bCryptPasswordEncoder));

        return new JoinRespDto(userPS);
    }
}
