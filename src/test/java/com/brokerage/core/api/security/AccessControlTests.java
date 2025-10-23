package com.brokerage.core.api.security;

import com.brokerage.core.api.asset.model.Asset;
import com.brokerage.core.api.asset.repository.AssetRepository;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.repository.CustomerRepository;
import com.brokerage.core.api.order.model.Order;
import com.brokerage.core.api.order.repository.OrderRepository;
import com.brokerage.core.base.enumaration.OrderSide;
import com.brokerage.core.base.enumaration.OrderStatus;
import com.brokerage.core.base.enumaration.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CustomerRepository customers;
    @Autowired PasswordEncoder encoder;
    @Autowired AssetRepository assets;
    @Autowired OrderRepository orders;

    String tokenAlice;
    String tokenBob;
    String tokenAdmin;
    UUID aliceId;
    UUID bobId;

    @BeforeEach
    void setup() throws Exception {
        orders.deleteAll();
        assets.deleteAll();
        customers.deleteAll();

        Customer alice = customers.save(Customer.builder()
                .username("alice")
                .password(encoder.encode("pass"))
                .role(Role.CUSTOMER).build());
        Customer bob = customers.save(Customer.builder()
                .username("bob")
                .password(encoder.encode("pass"))
                .role(Role.CUSTOMER).build());
        Customer admin = customers.save(Customer.builder()
                .username("admin")
                .password(encoder.encode("pass"))
                .role(Role.ADMIN).build());

        aliceId = alice.getId();
        bobId = bob.getId();

        assets.save(Asset.builder().customerId(aliceId).assetName("TRY").size(bd(100000)).usableSize(bd(100000)).build());
        assets.save(Asset.builder().customerId(bobId).assetName("TRY").size(bd(100000)).usableSize(bd(100000)).build());
        assets.save(Asset.builder().customerId(bobId).assetName("AAPL").size(bd(50)).usableSize(bd(50)).build());

        tokenAlice = loginAndGetAccessToken("alice", "pass");
        tokenBob   = loginAndGetAccessToken("bob", "pass");
        tokenAdmin = loginAndGetAccessToken("admin", "pass");
    }

    @Test
    void customer_createOrder_isForcedToSelf() throws Exception {
        String body = "{\n" +
                "  \"customerId\": \"" + bobId + "\",\n" +
                "  \"assetName\": \"AAPL\",\n" +
                "  \"orderSide\": \"BUY\",\n" +
                "  \"size\": 1.0,\n" +
                "  \"price\": 10.0\n" +
                "}";

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenAlice)
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(orders.findAll().stream().filter(o -> o.getCustomerId().equals(aliceId)).count()).isEqualTo(1);
        assertThat(orders.findAll().stream().filter(o -> o.getCustomerId().equals(bobId)).count()).isEqualTo(0);
    }

    @Test
    void customer_cannotCancelOthersOrder() throws Exception {
        // create order for bob directly
        Order o = orders.save(Order.builder()
                .customerId(bobId)
                .assetName("AAPL")
                .orderSide(OrderSide.SELL)
                .size(bd(5))
                .price(bd(10))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build());

        mvc.perform(delete("/api/orders/" + o.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied. You do not have permission to perform this action."));
    }

    @Test
    void customer_cannotViewOthersAssets() throws Exception {
        mvc.perform(get("/api/assets/" + bobId)
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied. You do not have permission to perform this action."));
    }

    @Test
    void admin_canActOnAny() throws Exception {
        // admin cancels Bob's pending order
        Order o = orders.save(Order.builder()
                .customerId(bobId)
                .assetName("AAPL")
                .orderSide(OrderSide.SELL)
                .size(bd(5))
                .price(bd(10))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build());

        mvc.perform(delete("/api/orders/" + o.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private String loginAndGetAccessToken(String username, String password) throws Exception {
        String body = "{\n  \"username\": \"" + username + "\",\n  \"password\": \"" + password + "\"\n}";
        String response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("data").get("accessToken").asText();
    }
}
