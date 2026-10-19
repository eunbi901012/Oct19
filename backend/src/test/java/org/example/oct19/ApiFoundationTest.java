package org.example.oct19;

import org.example.oct19.mapper.CatalogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Public health has a real mapper roundtrip; the production fallback never accepts invented credentials. */
@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
class ApiFoundationTest {
    @Autowired MockMvc http;
    @MockBean CatalogMapper mapper;

    @Test
    void healthReportsUpOnlyAfterDatabaseRoundtrip() throws Exception {
        when(mapper.ping()).thenReturn(1);
        http.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.status").value("UP"));
        verify(mapper).ping();
    }

    @Test
    void unavailableDatabaseDoesNotReportUp() throws Exception {
        when(mapper.ping()).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("offline"));
        http.perform(get("/api/health")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PERSISTENCE_ERROR"));
    }

    @Test
    void fallbackRejectsMissingOrArbitraryBearerWithoutDatabaseWrites() throws Exception {
        http.perform(put("/api/users").contentType("application/json")
                        .content("{\"user_id\":\"U\",\"enabled\":false}"))
                .andExpect(status().isUnauthorized());
        http.perform(get("/api/roles").header("Authorization", "Bearer unverified-test-input")
                        .param("page", "1").param("size", "10"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(mapper);
    }
}
