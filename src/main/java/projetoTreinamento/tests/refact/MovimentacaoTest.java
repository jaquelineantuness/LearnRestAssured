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

public class MovimentacaoTest extends BaseTest {

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
    public void deveInserirMovimentacaoComSucesso(){

        given()
                .body(getMovimentacaoValida())
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)

        ;
    }
    @Test
    public void deveValidarCamposObrigatoriosNaMovimentacao(){

        given()
                .body("{}")
        .when()
                .post("/transacoes")
        .then()
                .statusCode(400)
                .body("$", hasSize(8))
                .body("msg",hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório",
                        "Valor é obrigatório",
                        "Valor deve ser um número",
                        "Conta é obrigatório",
                        "Situação é obrigatório"
                ))

        ;
    }
    @Test
    public void naoDeveInserirMovimentacaoFutura(){
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao(DataUtils.getDataDiferencaDias(2));

        given()
                .body(mov)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(400)
                .body("$", hasSize(1))
                .body("msg", hasItems("Data da Movimentação deve ser menor ou igual à data atual"))

        ;
    }
    @Test
    public void naoDeveRemoverContaComMovimentacao(){

        given()
                .pathParam("id",getIdDaContaPeloNome("Conta com movimentacao"))
        .when()
                .delete("/contas/{id}")
        .then()
                .statusCode(500)
                .body("constraint",is("transacoes_conta_id_foreign"))
        ;
    }

    @Test
    public void deveRemoverMovimentacao(){

        given()
                .pathParam("id",getIdDaMovimentacaoPelaDescricao("Movimentacao para exclusao"))
        .when()
                .delete("/transacoes/{id}")
        .then()
                .statusCode(204)
        ;

    }

    public Integer getIdDaContaPeloNome(String nome){
        return RestAssured.get("/contas?nome"+nome).then().extract().path("id[0]");
    }
    public Integer getIdDaMovimentacaoPelaDescricao(String desc){
        return RestAssured.get("/transacoes?descricao"+desc).then().extract().path("id[0]");
    }

    private Movimentacao getMovimentacaoValida(){
        Movimentacao movimentacao = new Movimentacao();

        movimentacao.setConta_id(getIdDaContaPeloNome("Conta para movimentacoes"));
        movimentacao.setDescricao("Descrição da movimentacao");
        movimentacao.setEnvolvido("Envolvido na mov");
        movimentacao.setTipo("REC");
        movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        movimentacao.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        movimentacao.setValor(100f);
        movimentacao.setStatus(true);

        return movimentacao;
    }

}
