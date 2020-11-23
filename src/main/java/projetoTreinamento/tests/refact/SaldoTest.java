package projetoTreinamento.tests.refact;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Test;
import projetoTreinamento.core.BaseTest;
import projetoTreinamento.tests.Movimentacao;
import projetoTreinamento.utils.DataUtils;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.requestSpecification;
import static org.hamcrest.Matchers.*;

public class SaldoTest extends BaseTest {

    @BeforeClass
    public static void login(){

        Map<String, String> login = new HashMap<>();
        login.put("email", "antunes_jaqueline@yahoo.com.br");
        login.put("senha", "123456");

        String TOKEN =given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token")
                ;
        requestSpecification.header("Authorization","JWT "+ TOKEN);

        RestAssured.get("/reset").then().statusCode(200);
    }
    @Test
    public void deveCalcularSaldoContas(){

        Integer CONTA_ID = getIdDaContaPeloNome("Conta para saldo");

        given()

        .when()
                .get("/saldo")
        .then()
                .statusCode(200)
                .body("find{it.conta_id=="+CONTA_ID+"}.saldo", is("534.00"))
        ;
    }



    public Integer getIdDaContaPeloNome(String nome){
        return RestAssured.get("/contas?nome"+nome).then().extract().path("id[4]");
    }


}
