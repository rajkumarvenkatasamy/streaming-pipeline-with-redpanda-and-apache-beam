package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EnrichedUserActivity {

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("product_id")
    private int productId;
    @JsonProperty("state_description")
    private String stateDescription;

    @JsonProperty("region")
    private String region;

}