package praktikum;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ListOrdersTest {

    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private final String ORDERS_ENDPOINT = "/api/v1/orders";

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    @Step("Проверка, что в теле ответа возвращается список заказов")
    public void getOrderListReturnsOrdersTest() {
        int maxRetries = 3;

        // Попытки с повтором в случае ошибки 504
        retryRequestOn504(maxRetries);
    }

    @Step("Запрос списка заказов с повторением в случае 504")
    private void retryRequestOn504(int maxRetries) {
        int attempt = 0;
        boolean success = false;

        while (attempt < maxRetries && !success) {
            attempt++;
            System.out.println("Попытка №" + attempt + " получения списка заказов...");

            int statusCode =
                    given()
                            .contentType(ContentType.JSON)
                            .when()
                            .get(ORDERS_ENDPOINT)
                            .then()
                            .extract()
                            .statusCode();

            if (statusCode == 200) {
                System.out.println("Список заказов успешно получен на попытке №" + attempt);
                success = true;
            } else if (statusCode == 504) {
                System.out.println("Получен статус 504. Повторная попытка через " + (attempt * 2) + " секунды...");
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                System.out.println("Получен неожиданный статус: " + statusCode);
                break;
            }
        }

        if (!success) {
            throw new AssertionError("Не удалось получить список заказов после " + maxRetries + " попыток");
        }
    }
}
