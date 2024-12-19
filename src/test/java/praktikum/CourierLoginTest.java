package praktikum;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import models.Courier;


import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierLoginTest {

    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private final String COURIER_ENDPOINT = "/api/v1/courier";
    private final String LOGIN_ENDPOINT = "/api/v1/courier/login";
    private boolean needAuthorization = true;
    private int courierId;

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
        RestAssured.baseURI = BASE_URL;
        String login = generateRandomString();
        String password = generateRandomPassword();
        String firstName = generateRandomString();


        courier = new Courier(login, password, firstName);
    }

    @After
    public void tearDown() {
        if (needAuthorization) {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(courier)
                    .post(LOGIN_ENDPOINT);

            if (response.statusCode() == 200) {
                courierId = response.then().extract().path("id");
                deleteCourier(courierId);
            }
        }
    }


    @Step("Создание курьера")
    public void createCourier(Courier courier) {
        given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(COURIER_ENDPOINT)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Удаление курьера")
    private void deleteCourier(int courierId) {
        given()
                .contentType("application/json")
                .delete(COURIER_ENDPOINT + "/" + courierId)
                .then()
                .statusCode(200);
    }


    @Test
    public void existingCourierCanLoginTest() {
        createCourier(courier);
        loginWithExistingData();
    }

    @Step("Логин с существующим логином и паролем")
    public void loginWithExistingData() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"login\": \"" + courier.getLogin() + "\", \"password\": \"" + courier.getPassword() + "\" }")
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }


    @Test
    @Step("Авторизация без обязательных полей возвращает ошибку")
    public void loginWithoutRequiredFieldsReturnsErrorTest() {
        loginWithoutLoginData();
        loginWithoutPasswordData();
    }

    @Step("Авторизация без обязательного поля логин возвращает ошибку")
    public void loginWithoutLoginData() {
        needAuthorization = false;
        given()
                .contentType(ContentType.JSON)
                .body(getRequestBody(null, courier.getPassword()))
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Step("Авторизация без обязательного поля пароль возвращает ошибку")
    public void loginWithoutPasswordData() {
        needAuthorization = false; // Отключаем авторизацию в @After
        given()
                .contentType(ContentType.JSON)
                .body(getRequestBody(courier.getLogin(), null))
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(504);
    }

    public String getRequestBody(String login, String password) {
        StringBuilder body = new StringBuilder("{");
        if (login != null) {
            body.append("\"login\": \"").append(login).append("\", ");
        }
        if (password != null) {
            body.append("\"password\": \"").append(password).append("\", ");
        }
        // Убираем лишнюю запятую и пробел
        if (body.charAt(body.length() - 2) == ',') {
            body.delete(body.length() - 2, body.length());
        }
        body.append("}");
        return body.toString();
    }


    @Test
    public void incorrectLoginOrPasswordReturnsErrorTest() {
        createCourier(courier);
        loginWithWrongLogin();
        loginWithWrongPassword();

    }

    @Step("Авторизация с неправильным логином возвращает ошибку")
    public void loginWithWrongLogin() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"login\": \"" + "ABC123" + "\", \"password\": \"" + courier.getPassword() + "\" }")
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Step("Авторизация с неправильным паролем возвращает ошибку")
    public void loginWithWrongPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"login\": \"" + courier.getLogin() + "\", \"password\": \"" + "WrongPass" + "\" }")
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }


    @Test
    public void loginWithNonExistingUserReturnsErrorTest() {
        Courier nonExistentCourier = new Courier("nonexistent_login", "nonexistent_password", "NonExistentName");

        given()
                .contentType(ContentType.JSON)
                .body(nonExistentCourier)
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    public void successfulLoginReturnsIdTest() {
        createCourier(courier);
        retrieveIdAfterLogin();
    }
    @Step("Получение идентификационного номера курьера после успешного логина")
    public void retrieveIdAfterLogin(){
        int courierId = given()
                .contentType(ContentType.JSON)
                .body("{ \"login\": \"" + courier.getLogin() + "\", \"password\": \"" + courier.getPassword() + "\" }")
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().path("id");

        assert courierId > 0;
    }
}


