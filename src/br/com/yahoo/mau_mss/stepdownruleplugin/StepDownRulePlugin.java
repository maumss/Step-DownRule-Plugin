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
package br.com.yahoo.mau_mss.stepdownruleplugin;

import br.com.yahoo.mau_mss.stepdownruleplugin.domain.MethodScan;
import br.com.yahoo.mau_mss.stepdownruleplugin.domain.SourceScan;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.datatransfer.ExClipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

/**
 * Title: StepDownRulePlugin
 * Description: Plugin para organizar métodos segundo o Clean Code
 * Date: Jun 11, 2015, 9:57:24 AM
 *
 * @author Mauricio Soares da Silva (mauricio.soares)
 */
@ActionID(
        category = "File",
        id = "br.com.yahoo.mau_mss.stepdownruleplugin.StepDownRulePlugin"
)
@ActionRegistration(
        iconBase = "resources/news.png",
        displayName = "#CTL_StepDownRulePlugin"
)
@ActionReferences({
  @ActionReference(path = "Menu/Source", position = 2468),
  @ActionReference(path = "Editors/text/x-java/Popup", position = 1570)
})
@Messages("CTL_StepDownRulePlugin=Step-Down Rule")
public class StepDownRulePlugin implements ActionListener {
  private final DataObject context;
  private Clipboard clipboard;
  private List<MethodScan> methodScans;
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(StepDownRulePlugin.class.getName());

  public StepDownRulePlugin(DataObject context) {
    this.context = context;
    this.clipboard = Lookup.getDefault().lookup(ExClipboard.class);
    if (this.clipboard == null) {
      this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    FileObject fileObject = this.context.getPrimaryFile();
    if (fileObject == null) {
      StatusDisplayer.getDefault().setStatusText("The file is not open!");
      return;
    }
    StatusDisplayer.getDefault().setStatusText("Path: " + fileObject.getPath());
    logger.log(Level.INFO, "Examinando arquivo {0}", fileObject.getPath());
    JavaSource javaSource = JavaSource.forFileObject(fileObject);
    if (javaSource == null) {
      StatusDisplayer.getDefault().setStatusText("The file is not a Java source file!");
      return;
    }
    try {
      javaSource.runUserActionTask(new StepDownTask(), true);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Erro ao ordenar métodos.", ex);
      Exceptions.printStackTrace(ex);
    }
    if (this.methodScans.isEmpty()) {
      StatusDisplayer.getDefault().setStatusText("There is no methods in the source file!");
      logger.info("Nenhum método encontrado");
      return;
    }
    logger.log(Level.INFO, "M\u00e9todos encontrados: {0}", this.methodScans.toString());
    Collections.sort(this.methodScans);
    logger.log(Level.INFO, "M\u00e9todos ap\u00f3s ordena\u00e7\u00e3o: {0}", this.methodScans.toString());
    if (!MethodScan.hasModificacoes(this.methodScans)) {
      StatusDisplayer.getDefault().setStatusText("Your program already follows the Step-Down Rule. Congratulations!");
      return;
    }
    try {
      ModificationResult result = javaSource.runModificationTask(new MethodSorter());
      result.commit();
    } catch (IllegalArgumentException | IOException ex) {
      logger.log(Level.SEVERE, "Erro ao modificar código fonte.", ex);
      Exceptions.printStackTrace(ex);
    }
    try {
      javaSource.runUserActionTask(new FixBlankLines(), true);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Erro ao ajustar linhas em branco.", e);
      Exceptions.printStackTrace(e);
    }
  }

  private class StepDownTask implements CancellableTask<CompilationController> {

    @Override
    public void cancel() {
    }

    @Override
    public void run(CompilationController compilationController) throws Exception {
      compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
      Document document = compilationController.getDocument();
      if (document == null) {
        logger.info("O documento não pode ser verificado!");
        return;
      }
      new MemberVisitor(compilationController).scan(compilationController.getCompilationUnit(), null);
    }

  }

  private class MemberVisitor extends TreePathScanner<Void, Void> {
    private final CompilationInfo info;

    MemberVisitor(CompilationInfo info) {
      this.info = info;
    }

    @Override
    public Void visitClass(ClassTree classTree, Void v) {
      Element el = info.getTrees().getElement(getCurrentPath());
      if (el == null) {
        logger.info("Não é possível resolver a classe!");
        return null;
      }
      TypeElement te = (TypeElement) el;
      @SuppressWarnings("unchecked")
      List<Element> enclosedElements = (List<Element>) te.getEnclosedElements();
      methodScans = new ArrayList<>();
      int i = 0;
      for (Element enclosedElement : enclosedElements) {
        logger.log(Level.INFO, "Elemento {0} = {1}", new Object[]{i, enclosedElement.getSimpleName().toString()});
        if (enclosedElement.getKind() == ElementKind.METHOD) {
          MethodTree methodTree = (MethodTree) info.getTrees().getTree(enclosedElement);
          methodScans.add(new MethodScan(i, enclosedElement.getSimpleName().toString(), methodTree));
        }
        i++;
      }
      for (MethodScan method : methodScans) {
        method.identificaInvocacoesDeMetodos(classTree.getSimpleName().toString(), methodScans);
      }
      return null;
    }
  }

  private class MethodSorter implements CancellableTask<WorkingCopy> {

    @Override
    public void cancel() {
    }

    /**
     * Executa modificação nos métodos encontrados no fonte
     *
     * @see:
     * http://bits.netbeans.org/6.7/javadoc/org-netbeans-modules-java-source/org/netbeans/api/java/source/TreeMaker.html
     */
    @Override
    public void run(WorkingCopy workingCopy) throws Exception {
      workingCopy.toPhase(Phase.RESOLVED);
      CompilationUnitTree cut = workingCopy.getCompilationUnit();
      TreeMaker make = workingCopy.getTreeMaker();
      for (Tree typeDecl : cut.getTypeDecls()) {
        if (Tree.Kind.CLASS == typeDecl.getKind()) {
          ClassTree clazz = (ClassTree) typeDecl;
          ClassTree modifiedClazz = clazz;
          int numeroItens = modifiedClazz.getMembers().size();
          int posMetodo = -1;
          for (int i = numeroItens - 1; i > -1; i--) {
            Tree member = modifiedClazz.getMembers().get(i);
            if (member.getKind() == Tree.Kind.METHOD) {
              MethodTree methodMember = (MethodTree) member;
              if (MethodScan.contains(methodScans, methodMember.getName().toString())) {
                logger.log(Level.INFO, "A \u00e1rvore tem {0} elementos e ir\u00e1 remover o item {1}({2})",
                        new Object[]{modifiedClazz.getMembers().size(), i, methodMember.getName().toString()});
                modifiedClazz = make.removeClassMember(modifiedClazz, i);
                posMetodo = i;
              }
            }
          }
          for (MethodScan methodScan : methodScans) {
            logger.log(Level.INFO, "Vai inserir m\u00e9todo na posi\u00e7\u00e3o {0}", posMetodo);
            modifiedClazz = make.insertClassMember(modifiedClazz, posMetodo, methodScan.getMethodTree());
            posMetodo++;
          }
          workingCopy.rewrite(clazz, modifiedClazz);
        }
      }
    }
  }

  private class FixBlankLines implements CancellableTask<CompilationController> {

    @Override
    public void cancel() {
    }

    @Override
    public void run(CompilationController compilationController) throws Exception {
      compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
      Document document = compilationController.getDocument();
      if (document == null) {
        return;
      }
      List<SourceScan> sourceScans = loadSourceCode(document);
      updateBlankLinesAfterLastMethod(sourceScans);
      updateBlankLinesBetweenMethods(sourceScans);
      addOrRemoveLines(document, sourceScans);
    }

    private List<SourceScan> loadSourceCode(Document document)
            throws BadLocationException {
      List<SourceScan> sourceScans = new ArrayList<>();
      javax.swing.text.Element rootElement = document.getDefaultRootElement();
      int numberOfLines = document.getDefaultRootElement().getElementCount();
      for (int i = 0; i < numberOfLines; i++) {
        javax.swing.text.Element lineElement = rootElement.getElement(i);
        int lineStartOffset = lineElement.getStartOffset();
        int lineEndOffset = lineElement.getEndOffset();
        String linha = document.getText(lineStartOffset, (lineEndOffset - lineStartOffset));
        sourceScans.add(new SourceScan(linha, isMethod(linha), lineStartOffset));
        logger.log(Level.FINEST, String.format("linesText[%d] (offset %d, length %d = [%s].", i + 1,
                lineStartOffset, (lineEndOffset - lineStartOffset), linha));
      }
      return sourceScans;
    }

    private void updateBlankLinesBetweenMethods(List<SourceScan> sourceScans) {
      boolean passouPeloNomeDoMetodo = false;
      int numeroLinhasEmBranco = 0;
      for (int i = sourceScans.size() - 1; i > -1; i--) {
        SourceScan sourceScan = sourceScans.get(i);
        if (sourceScan.isMethod()) {
          passouPeloNomeDoMetodo = true;
          logger.log(Level.FINE, String.format("Encontrou um método: [%s].", sourceScan.getText()));
          continue;
        }
        String text = removeBlanks(sourceScan.getText());
        text = removeBlanks(text);
        if (text.isEmpty()) {
          numeroLinhasEmBranco++;
          if (numeroLinhasEmBranco > 1) {
            sourceScan.setRemove(true);
          }
          continue;
        }
        // primeira chaves ou ponto e vírgula logo após encontrar um método
        if (passouPeloNomeDoMetodo && (text.contains("}") || text.contains(";"))) {
          if (numeroLinhasEmBranco == 0) {
            sourceScan.setAppend(true);
          }
          passouPeloNomeDoMetodo = false;
        }
        numeroLinhasEmBranco = 0;
      }
    }

    private void updateBlankLinesAfterLastMethod(List<SourceScan> sourceScans) {
      boolean encontrouFinalDaClasse = false;
      int numeroLinhasEmBranco = 0;
      for (int i = sourceScans.size() - 1; i > -1; i--) {
        SourceScan sourceScan = sourceScans.get(i);
        String text = removeBlanks(sourceScan.getText());
        if (!encontrouFinalDaClasse && text.contains("}")) {
          encontrouFinalDaClasse = true;
          continue;
        }
        if (encontrouFinalDaClasse) {
          if (text.isEmpty()) {
            numeroLinhasEmBranco++;
            if (numeroLinhasEmBranco > 1) {
              sourceScan.setRemove(true);
            }
          }
          if (text.contains("}")) {
            break;
          }
        }
      }
    }

    private void addOrRemoveLines(Document document, List<SourceScan> sourceScans) throws BadLocationException {
      String lineEnd = (String) document.getProperty(DefaultEditorKit.EndOfLineStringProperty);
      for (int i = sourceScans.size() - 1; i > -1; i--) {
        SourceScan source = sourceScans.get(i);
        if (source.isAppend()) {
          logger.info(String.format("Linha [%d], de tamanho %d - vai incluir nova linha logo abaixo: [%s]",
                  i, source.getText().length(), removeBlanks(source.getText())));
          document.insertString(source.getOffset() + source.getText().length(), lineEnd, null);
        }
        if (source.isRemove()) {
          logger.info(String.format("Linha [%d], de tamanho %d - vai ser excluída: [%s]",
                  i, source.getText().length(), removeBlanks(source.getText())));
          document.remove(source.getOffset(), source.getText().length());
        }
      }
    }

    private boolean isMethod(String lineOfCode) {
      String text = removeBlanks(lineOfCode);
      if (text.isEmpty()) {
        return false;
      }
      for (MethodScan methodScan : methodScans) {
        if (text.contains(methodScan.getReturnType() + methodScan.getName())) {
          return true;
        }
      }
      return false;
    }

    private String removeBlanks(String text) {
      if (text == null) {
        return "";
      }
      return text.replaceAll("\\s", "");
    }
  }
}
