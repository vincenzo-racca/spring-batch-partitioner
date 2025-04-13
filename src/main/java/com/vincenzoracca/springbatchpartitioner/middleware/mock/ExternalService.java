package com.vincenzoracca.springbatchpartitioner.middleware.mock;


import java.util.List;


public class ExternalService {

    private ExternalService() {}

    public static List<String> fetchProvinces() {
        return List.of(
                "BA",
                "MI",
                "NA",
                "RM"
        );
    }

    public static List<String> fetchDeliveryDrivers() {
        return List.of(
                "1",
                "2"
        );
    }

    public static String buildPks() {
        List<String> provinces = ExternalService.fetchProvinces();
        List<String> deliveryDrivers = ExternalService.fetchDeliveryDrivers();
        StringBuilder pks = new StringBuilder();
        for (String deliveryDriver : deliveryDrivers) {
            for (String province : provinces) {
                pks.append(deliveryDriver).append("##").append(province).append(",");
            }
        }

        return pks.deleteCharAt(pks.length() - 1).toString();
    }
}
