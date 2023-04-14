package org.example;

import java.util.HashMap;

public class StateAndRegion {

    private HashMap<String, String> stateRegionMap = new HashMap<>();
    private HashMap<String, String> stateCodeAndDescriptionMap = new HashMap<>();
    StateAndRegion() {
        stateRegionMap.put("TN", "South");
        stateRegionMap.put("AP", "South");
        stateRegionMap.put("KL", "South");
        stateRegionMap.put("KA", "South");
        stateRegionMap.put("PY", "South");
        stateRegionMap.put("DL", "North");
        stateRegionMap.put("RJ", "North");
        stateRegionMap.put("UK", "North");
        stateRegionMap.put("UP", "North");
        stateRegionMap.put("HP", "North");


        stateCodeAndDescriptionMap.put("TN", "Tamil Nadu");
        stateCodeAndDescriptionMap.put("AP", "Andhra Pradesh");
        stateCodeAndDescriptionMap.put("KL", "Kerala");
        stateCodeAndDescriptionMap.put("KA", "Karnataka");
        stateCodeAndDescriptionMap.put("PY", "Pondicherry");
        stateCodeAndDescriptionMap.put("DL", "Delhi");
        stateCodeAndDescriptionMap.put("RJ", "Rajasthan");
        stateCodeAndDescriptionMap.put("UK", "Uttarakhand");
        stateCodeAndDescriptionMap.put("UP", "Uttar Pradesh");
        stateCodeAndDescriptionMap.put("HP", "Himachal Pradesh");
    }
    public String getRegionByStateCode(String stateCode) {
        return stateRegionMap.get(stateCode);
    }

    public String getStateDescriptionByStateCode(String stateCode) {
        return stateCodeAndDescriptionMap.get(stateCode);
    }
}
