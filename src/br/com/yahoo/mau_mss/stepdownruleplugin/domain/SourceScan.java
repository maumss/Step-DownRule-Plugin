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

/**
 * Title: SourceScan
 * Description: Cria um resumo de cada linha dentro do código fonte analisado
 * Date: Jun 30, 2015, 2:52:25 PM
 *
 * @author Mauricio Soares da Silva (mauricio.soares)
 */
public class SourceScan {
  private String text;
  private boolean method;
  private int offset;
  private boolean remove;
  private boolean append;

  /**
   * Create a new instance of <code>SourceScan</code>.
   */
  public SourceScan() {
  }

  public SourceScan(String text, boolean method, int offset) {
    this.text = text;
    this.method = method;
    this.offset = offset;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean isMethod() {
    return method;
  }

  public void setMethod(boolean method) {
    this.method = method;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public boolean isRemove() {
    return remove;
  }

  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  public boolean isAppend() {
    return append;
  }

  public void setAppend(boolean append) {
    this.append = append;
  }

}
