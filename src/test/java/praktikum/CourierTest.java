package praktikum;

import io.restassured.RestAssured;
import models.Courier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.UUID;
import java.util.Random;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import io.qameta.allure.Step;

public class CourierTest {

    private Courier courier;

    //Рандомайзер логина
    private String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Создание рандомного пароля
    private String generateRandomPassword() {
        Random random = new Random();
        int length = 10; // Length of the password
        StringBuilder password = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        String login = generateRandomString();  // Unique login
        String password = generateRandomPassword();  // Random password
        String firstName = generateRandomString();  // Random first name

        // Create the courier object with dynamically generated data
        courier = new Courier(login, password, firstName);
    }

    @After
    public void tearDown() {
    }


    @Test
    public void createCourierTest() {
        given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }


    @Test
    public void createCourierWithExistingLoginTest() {
        createCourier(courier);
        createDuplicateCourier(courier);
    }

    @Step("Создание курьера")
    private void createCourier(Courier courier) {
        given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201) // По условиям задачи firstName не является обязательным
                .body("ok", equalTo(true));
    }

    @Step("Создание дупликата курьера")
    private void createDuplicateCourier(Courier courier) {
        given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409) // Код ошибки 409 (Conflict)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }



    @Test
    public void createCourierWithoutLoginTest() {
        Courier invalidCourier = new Courier(null, generateRandomPassword(), generateRandomString());

        given()
                .header("Content-type", "application/json")
                .body(invalidCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400) // Проверяем, что код ответа 400
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }


    @Test
    public void createCourierWithoutPasswordTest() {
        Courier invalidCourier = new Courier(generateRandomString(), null, generateRandomString());

        given()
                .header("Content-type", "application/json")
                .body(invalidCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400) // Проверяем, что код ответа 400
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }


    @Test
    public void createCourierWithoutFirstNameTest() {
        Courier invalidCourier = new Courier(generateRandomString(), generateRandomPassword(), null);

        given()
                .header("Content-type", "application/json")
                .body(invalidCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201) // Поле firstName не является обязательным
                .body("ok", equalTo(true));
    }

}

