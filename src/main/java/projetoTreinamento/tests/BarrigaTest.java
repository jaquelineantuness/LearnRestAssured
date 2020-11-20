package projetoTreinamento.tests;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import projetoTreinamento.core.BaseTest;
import projetoTreinamento.utils.DataUtils;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest {

    private String TOKEN;
    private static String CONTA_NAME = "Conta " + System.nanoTime();
    private static Integer CONTA_ID;
    private static Integer MOVIMENTACAO_ID;

    @Before
    public void login(){
        Map<String, String> login = new HashMap<>();
        login.put("email", "antunes_jaqueline@yahoo.com.br");
        login.put("senha", "123456");

         TOKEN =given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token")
                ;
    }

    @Test
    public void t01_naoDeveAcessarAPISemToken(){
        given()
        .when()
                .get("/contas")
        .then()
                .statusCode(401)
        ;
    }

    @Test
    public void t_02_deveIncluirContaComSucesso(){


        CONTA_ID = given()
                .header("Authorization","JWT "+ TOKEN)
                .body("{\"nome\":\""+ CONTA_NAME +"\"}")
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id");
        ;
    }
    @Test
    public void t_03_deveAlterarContaComSucesso(){

        given()
                .header("Authorization","JWT "+ TOKEN)
                .body("{\"nome\":\""+ CONTA_NAME +" alterada\"}")
                .pathParam("id",CONTA_ID)
        .when()
                .put("/contas/{id}")
        .then()
                .statusCode(200)
                .body("nome", is(CONTA_NAME +" alterada"))
        ;
    }

    @Test
    public void t_04_naoDeveInserirContaComMesmoNome(){
            given()
                .header("Authorization","JWT "+ TOKEN)
                .body("{\"nome\":\""+ CONTA_NAME +" alterada\"}")
            .when()
                .post("/contas")
            .then()
                .statusCode(400)
                .body("error",is("Já existe uma conta com esse nome!"))
            ;
    }
    @Test
    public void t_05_deveInserirMovimentacaoComSucesso(){

        MOVIMENTACAO_ID =given()
                .header("Authorization","JWT "+ TOKEN)
                .body(getMovimentacaoValida())
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)
                .extract().path("id")

        ;
    }

    @Test
    public void t_06_deveValidarCamposObrigatoriosNaMovimentacao(){

        given()
                .header("Authorization","JWT "+ TOKEN)
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
    public void t_07_naoDeveInserirMovimentacaoFutura(){
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao(DataUtils.getDataDiferencaDias(2));

        given()
                .header("Authorization","JWT "+ TOKEN)
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
    public void t_08_naoDeveRemoverContaComMovimentacao(){

        given()
                .header("Authorization","JWT "+ TOKEN)
                .pathParam("id",CONTA_ID)
        .when()
                .delete("/contas/{id}")
        .then()
                .statusCode(500)
                .body("constraint",is("transacoes_conta_id_foreign"))
        ;
    }
    @Test
    public void t_09_deveCalcularSaldoContas(){

        given()
                .header("Authorization","JWT "+ TOKEN)
        .when()
                .get("/saldo")
        .then()
                .statusCode(200)
                .body("find{it.conta_id=="+CONTA_ID+"}.saldo", is("100.00"))
        ;
    }
    @Test
    public void t_10_deveRemoverMovimentacao(){

        given()
                .header("Authorization","JWT "+ TOKEN)
                .pathParam("id",MOVIMENTACAO_ID)
        .when()
                .delete("/transacoes/{id}")
        .then()
                .statusCode(204)
        ;

    }

    private Movimentacao getMovimentacaoValida(){
        Movimentacao movimentacao = new Movimentacao();

        movimentacao.setConta_id(CONTA_ID);
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
