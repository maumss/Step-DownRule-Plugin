package br.com.yahoo.mau_mss.stepdownruleplugin.domain;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mauricio.soares
 */
public class MethodScanTest {

  public MethodScanTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Testa a ordenação dos métodos.
   * A estrutura antes da ordenação:
   * <code>
   * void methodD() { }
   * void methodB() { }
   * void methodA() {methodB(); methodC();}
   * void methodC() {methodD();}
   * </code>
   *
   * Deve ficar assim:
   * <code>
   * void methodA() {methodB(); methodC();}
   * void methodB() { }
   * void methodC() {methodD();}
   * void methodD() { }
   * </code>
   */
  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    List<MethodScan> methods = new ArrayList<>();
    methods.add(new MethodScan(0, "methodD", null));
    methods.add(new MethodScan(1, "methodB", null));
    MethodScan methodScan = new MethodScan(2, "methodA", null);
    methodScan.setCalls(Arrays.asList("methodB", "methodC"));
    methods.add(methodScan);
    methodScan = new MethodScan(3, "methodC", null);
    methodScan.setCalls(Arrays.asList("methodD"));
    methods.add(methodScan);
    System.out.println("Fonte antes da ordenacao:");
    for (MethodScan currentMethod : methods) {
      System.out.println("void " + currentMethod.getName() + "() {" + currentMethod.getCalls().toString() + "}");
    }
    System.out.println("");
    System.out.println("Fonte depois da ordenacao:");
    Collections.sort(methods);
    for (MethodScan currentMethod : methods) {
      System.out.println("void " + currentMethod.getName() + "() {" + currentMethod.getCalls().toString() + "}");
    }
    assertEquals("methodD", methods.get(3).getName());
  }

  @Test
  public void testIdentificaInvocacoesDeMetodos() {
    System.out.println("identificaInvocacoesDeMetodos");
    List<MethodScan> methodScans = new ArrayList<>();
    MethodScan methodScan = new MethodScan(0, "initListeners", null);
    methodScan.setBody("this.btOk.setOnAction((ActionEvent event) -> \n"
            + "      checkFile()\n"
            + "    );\n"
            + "    this.btVoltar.setOnAction((ActionEvent event) -> \n"
            + "      voltar()\n"
            + "    );\n"
            + "    this.btArquivo.setOnAction((final ActionEvent event) -> \n"
            + "      selecionarArquivo()\n"
            + "    );");
    methodScans.add(methodScan);
    methodScans.add(new MethodScan(1, "selecionarArquivo", null));
    methodScan.identificaInvocacoesDeMetodos("ClasseTeste", methodScans);
    String expResult = "selecionarArquivo";
    String result = methodScan.getCalls().get(0);
    assertEquals(expResult, result);
  }

}
