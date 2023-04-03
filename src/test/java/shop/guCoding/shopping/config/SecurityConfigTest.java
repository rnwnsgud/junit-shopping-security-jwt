package shop.guCoding.shopping.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@AutoConfigureMockMvc // Mock, 가짜환경에 MockMvc가 등록됨
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // 통합테스트가 가짜환경에서 진행됨
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void authentication_test() throws Exception {
        //given
       
        //when
        ResultActions resultActions = mvc.perform(get("/api/s/hi"));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);
        int httpStatusCode = resultActions.andReturn().getResponse().getStatus();
        //then
        assertThat(httpStatusCode).isEqualTo(401);
    }



}