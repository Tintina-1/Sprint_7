package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Order {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("metroStation")
    private String metroStation;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("rentTime")
    private int rentTime;

    @JsonProperty("deliveryDate")
    private String deliveryDate;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("color")
    private List<String> color;

    // Конструктор для всех полей
    public Order(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, List<String> color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
    }

    // Обязательно нужен пустой конструктор для Jackson
    public Order() {}
}
