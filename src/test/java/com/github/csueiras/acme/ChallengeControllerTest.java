package com.github.csueiras.acme;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the {@link ChallengeController}
 *
 * @author Christian Sueiras
 */
@WebMvcTest
@Import(ChallengeController.class)
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChallengeStore mockStore;

    @Test
    void challenge() throws Exception {
        when(mockStore.get("theToken")).thenReturn("authorizationCode");

        mockMvc.perform(MockMvcRequestBuilders.get("/.well-known/acme-challenge/{token}", "theToken"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("authorizationCode"));
    }
}