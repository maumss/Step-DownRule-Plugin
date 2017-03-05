/*
 * Copyright 2015 Mauricio Soares da Silva.
 *
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Tradução não-oficial:
 *
 * Este programa é um software livre; você pode redistribuí-lo e/ou
 *   modificá-lo dentro dos termos da Licença Pública Geral GNU como
 *   publicada pela Fundação do Software Livre (FSF); na versão 3 da
 *   Licença, ou (na sua opinião) qualquer versão.
 *
 *   Este programa é distribuído na esperança de que possa ser útil,
 *   mas SEM NENHUMA GARANTIA; sem uma garantia implícita de ADEQUAÇÃO
 *   a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a
 *   Licença Pública Geral GNU para maiores detalhes.
 *
 *   Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 *   com este programa. Se não, veja <http://www.gnu.org/licenses/>.
 */
package br.com.yahoo.mau_mss.stepdownruleplugin.domain;

import com.sun.source.tree.MethodTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: MethodScan
 * Description: Classe que representa o método do fonte e suas chamadas a outros métodos
 * Date: Jun 11, 2015, 9:58:24 AM
 *
 * @author Mauricio Soares da Silva (mauricio.soares)
 */
public class MethodScan implements Comparable<MethodScan> {
  private int id;
  private String name;
  private List<String> calls;
  private String returnType;
  private String body;
  private MethodTree methodTree;

  public MethodScan(int id, String name, MethodTree methodTree) {
    this.id = id;
    this.name = name;
    this.calls = new ArrayList<>();
    this.body = "";
    this.methodTree = methodTree;
    if (methodTree != null && methodTree.getBody() != null) {
      this.body = methodTree.getBody().toString();
      this.returnType = methodTree.getReturnType().toString();
    }
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCalls() {
    return calls;
  }

  public void setCalls(List<String> calls) {
    this.calls = new ArrayList<>(calls);
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public MethodTree getMethodTree() {
    return methodTree;
  }

  public void setMethodTree(MethodTree methodTree) {
    this.methodTree = methodTree;
  }

  @Override
  public int compareTo(MethodScan o) {
    if (this.chama(o.getName())) {
      return -1;
    }
    if (o.chama(this.getName())) {
      return 1;
    }
    return this.getName().compareTo(o.getName());
  }

  private boolean chama(String calling) {
    return this.calls.contains(calling);
  }

  public void identificaInvocacoesDeMetodos(String className, List<MethodScan> methods) {
    String bodyWithotSpaces = getBody().replaceAll("\\s", "");
    if (!getCalls().isEmpty() || bodyWithotSpaces.isEmpty()) {
      return;
    }
    for (MethodScan method : methods) {
      String currentName = method.getName();
      if (getName().equals(currentName)) {
        continue;
      }
      String methodCall = currentName + "(";
      if (bodyWithotSpaces.contains(methodCall)) {
        if (comecaComPonto(bodyWithotSpaces, methodCall)
                && (naoComecaComThisPonto(bodyWithotSpaces, methodCall)
                || naoComecaComNomeDaClasse(bodyWithotSpaces, methodCall, className))) {
          continue;
        }
        getCalls().add(currentName);
      }
    }
  }

  private boolean comecaComPonto(String methodBody, String methodCall) {
    int pos = methodBody.indexOf(methodCall);
    return (pos > 1 && methodBody.substring(pos - 1, pos).equals("."));
  }

  private boolean naoComecaComThisPonto(String methodBody, String methodCall) {
    int pos = methodBody.indexOf(methodCall);
    return !(pos > 5 && methodBody.substring(pos - 5, pos).equals("this."));
  }

  private boolean naoComecaComNomeDaClasse(String methodBody, String methodCall, String className) {
    int pos = methodBody.indexOf(methodCall);
    int tamanhoNomeDaClassePonto = className.length() + 1;
    return !(pos > tamanhoNomeDaClassePonto && methodBody.substring(pos - tamanhoNomeDaClassePonto, pos).equals(className + "."));
  }

  public static boolean hasModificacoes(List<MethodScan> methods) {
    if (methods == null || methods.isEmpty()) {
      return false;
    }
    int i = 0;
    for (MethodScan method : methods) {
      if (method.getId() < i) {
        return true;
      }
      i = method.getId();
    }
    return false;
  }

  public static boolean contains(List<MethodScan> methods, String methodName) {
    if (methods == null || methods.isEmpty() || methodName == null) {
      return false;
    }
    for (MethodScan method : methods) {
      if (method.getName().equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder resumo = new StringBuilder();
    resumo.append(this.name);
    resumo.append("[");
    if (this.calls != null && !this.calls.isEmpty()) {
      int total = this.calls.size();
      for (String metodo : this.calls) {
        resumo.append(metodo);
        total--;
        if (total > 0) {
          resumo.append(", ");
        }
      }
    }
    resumo.append("]");
    return resumo.toString();
  }

}
