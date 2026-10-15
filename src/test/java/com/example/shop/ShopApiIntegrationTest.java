package com.example.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShopApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void customerLifecycleUsesRestfulStatusCodes() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Phạm Thu Hà","email":"ha.pham@example.com","phone":"0911222333"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/customers/\\d+$")))
                .andExpect(jsonPath("$.email").value("ha.pham@example.com"))
                .andReturn();
        long id = idOf(created);

        mockMvc.perform(patch("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Hà Nội\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Hà Nội"))
                .andExpect(jsonPath("$.fullName").value("Phạm Thu Hà"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Trùng Email\",\"email\":\"HA.PHAM@example.com\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

        mockMvc.perform(get("/api/v1/customers").param("page", "0").param("size", "5").param("sort", "fullName,desc")
                        .param("keyword", "ha.pham"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void validationErrorsUseUnifiedErrorShape() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"A\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details", hasSize(2)));

        mockMvc.perform(get("/api/v1/products").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        mockMvc.perform(get("/api/v1/products").param("sort", "secret,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT_FIELD"));
    }

    @Test
    void orderCalculatesTotalManagesStockAndStatus() throws Exception {
        long customerId = idOf(mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Đỗ Quang\",\"email\":\"quang.do@example.com\",\"address\":\"Đà Nẵng\"}"))
                .andExpect(status().isCreated()).andReturn());
        long productId = idOf(mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bàn phím cơ\",\"price\":1500000,\"stockQuantity\":5,\"category\":\"Accessory\"}"))
                .andExpect(status().isCreated()).andReturn());

        String orderBody = """
                {"customerId":%d,"items":[{"productId":%d,"quantity":2}]}
                """.formatted(customerId, productId);
        long orderId = idOf(mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON).content(orderBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.totalAmount").value(3000000))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.shippingAddress").value("Đà Nẵng"))
                .andReturn());

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(jsonPath("$.stockQuantity").value(3));

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":%d,"items":[{"productId":%d,"quantity":10}]}
                                """.formatted(customerId, productId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        mockMvc.perform(get("/api/v1/customers/{id}/orders", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(patch("/api/v1/orders/{id}", orderId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        mockMvc.perform(patch("/api/v1/orders/{id}", orderId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(jsonPath("$.stockQuantity").value(5));

        mockMvc.perform(delete("/api/v1/customers/{id}", customerId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CUSTOMER_HAS_ORDERS"));

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    private long idOf(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
