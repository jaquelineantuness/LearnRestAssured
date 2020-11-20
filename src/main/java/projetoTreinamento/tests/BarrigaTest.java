package projetoTreinamento.tests;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import projetoTreinamento.core.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BarrigaTest extends BaseTest {
    private String TOKEN;

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
    public void naoDeveAcessarAPISemToken(){
        given()
        .when()
                .get("/contas")
        .then()
                .statusCode(401)
        ;
    }

    @Test
    public void deveIncluirContaComSucesso(){


        given()
                .header("Authorization","JWT "+ TOKEN)
                .body("{\"nome\":\"conta qualquer\"}")
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
        ;
    }
    @Test
    public void deveAlterarContaComSucesso(){

        given()
                .header("Authorization","JWT "+ TOKEN)
                .body("{\"nome\":\"conta alterada\"}")
        .when()
                .put("/contas/326029")
        .then()
                .statusCode(200)
                .body("nome", is("conta alterada"))
        ;
    }

    @Test
    public void naoDeveInserirContaComMesmoNome(){
            given()
                .header("Authorization","JWT "+ TOKEN)
                .body("{\"nome\":\"conta alterada\"}")
            .when()
                .post("/contas")
            .then()
                .statusCode(400)
                .body("error",is("Já existe uma conta com esse nome!"))
            ;
    }
    @Test
    public void deveInserirMovimentacaoComSucesso(){

        given()
                .header("Authorization","JWT "+ TOKEN)
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
    public void naoDeveInserirMovimentacaoFutura(){
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao("20/10/2021");

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
    public void naoDeveRemoverContaComMovimentacao(){

        given()
                .header("Authorization","JWT "+ TOKEN)
        .when()
                .delete("/contas/326029")
        .then()
                .statusCode(500)
                .body("constraint",is("transacoes_conta_id_foreign"))
        ;
    }
    @Test
    public void deveCalcularSaldoContas(){

        given()
                .header("Authorization","JWT "+ TOKEN)
        .when()
                .get("/saldo")
        .then()
                .statusCode(200)
                .body("find{it.conta_id==326029}.saldo", is("200.00"))
        ;
    }
    @Test
    public void deveRemoverMovimentacao(){

        given()
                .header("Authorization","JWT "+ TOKEN)
        .when()
                .delete("/transacoes/296134")
        .then()
                .statusCode(204)
        ;

    }

    private Movimentacao getMovimentacaoValida(){
        Movimentacao movimentacao = new Movimentacao();

        movimentacao.setConta_id(326029);
        movimentacao.setDescricao("Descrição da movimentacao");
        movimentacao.setEnvolvido("Envolvido na mov");
        movimentacao.setTipo("REC");
        movimentacao.setData_transacao("01/01/2000");
        movimentacao.setData_pagamento("10/05/2020");
        movimentacao.setValor(100f);
        movimentacao.setStatus(true);

        return movimentacao;
    }
}
