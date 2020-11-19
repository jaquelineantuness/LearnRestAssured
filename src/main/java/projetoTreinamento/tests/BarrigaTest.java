package projetoTreinamento.tests;

import org.junit.Test;
import projetoTreinamento.core.BaseTest;

import static io.restassured.RestAssured.*;

public class BarrigaTest extends BaseTest {

    @Test
    public void naoDeveAcessarAPISemToken(){
        given()
        .when()
                .get("/contas")
        .then()
                .statusCode(401)
        ;
    }
}
