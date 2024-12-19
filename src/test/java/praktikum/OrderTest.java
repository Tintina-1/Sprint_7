package praktikum;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderTest {

    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private final String ORDER_ENDPOINT = "/api/v1/orders";
    private int orderTrack;
    private List<String> color;

    // Параметры для каждого теста — один, оба или отсутствие цветов
    @Parameterized.Parameters
    public static Collection<Object[]> getTestData() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList("BLACK")},
                {Arrays.asList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {Arrays.asList()}
        });
    }


    private final String firstName = "Дмитрий";
    private final String lastName = "Пешков";
    private final String address = "ул. Пушкинская, 135";
    private final String metroStation = "Комсомольская";
    private final String phone = "+79186059458";
    private final int rentTime = 2;
    private final String deliveryDate = "2024-12-19";
    private final String comment = "Привезите как можно быстрее";


    public OrderTest(List<String> color) {
        this.color = color;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @After
    public void tearDown() {
        if (orderTrack > 0) {
            cancelOrder(orderTrack);
        }
    }

    @Test
    @Step("Создание заказа с цветом {0}")
    public void createOrderTest() {
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
        orderTrack = createOrder(order);
    }

    @Step("Создание заказа")
    public int createOrder(Order order) {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(201) // Проверяем, что статус код 201
                .body("track", notNullValue()) // Проверяем, что track присутствует
                .extract().response();

        int track = response.path("track");
        System.out.println("Создан заказ с трек-номером: " + track);
        return track;
    }

    @Step("Удаление заказа с track {0}")
    public void cancelOrder(int track) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .put(ORDER_ENDPOINT + "/cancel?track=" + track)
                .then()
                .statusCode(200); // Убедимся, что заказ успешно удалён
        System.out.println("Удалён заказ с трек-номером: " + track);
    }
}
