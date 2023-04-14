package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserActivity {

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("product_id")
    private int productId;
    @JsonProperty("state_code")
    private String stateCode;

}