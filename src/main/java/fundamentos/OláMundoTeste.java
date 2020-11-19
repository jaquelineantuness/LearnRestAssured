package fundamentos;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.Assert;
import org.junit.Test;

public class OláMundoTeste {

   @Test
   public void testeOlaMundo(){

      Response response = RestAssured.request(Method.GET,"http://restapi.wcaquino.me:80/ola");
      Assert.assertTrue(response.getBody().asString().equals("Ola Mundo!"));
      Assert.assertTrue(response.statusCode()==200);
      Assert.assertTrue("O Status code deveria ser 201",response.statusCode()==200);
      Assert.assertEquals(200,response.statusCode());

      ValidatableResponse validacao = response.then();
      validacao.statusCode(200);

   }
}
