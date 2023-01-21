package cf.grcq.processor;

import static lombok.javac.handlers.JavacHandlerUtil.chainDots;
import static lombok.javac.handlers.JavacHandlerUtil.injectMethod;

import cf.grcq.processor.annotations.Database;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Modifier;
import java.util.Collection;

@MetaInfServices(JavacAnnotationHandler.class)
public class DatabaseHandler extends JavacAnnotationHandler<Database> {

    @Override
    public void handle(AnnotationValues<Database> annotation, JCAnnotation ast, JavacNode annotationNode) {
        JavacNode node = annotationNode.up();

        injectMethod(node, createTest(node));
    }

    private JCMethodDecl createTest(JavacNode node) {
        TreeMaker treeMaker = node.getTreeMaker().getUnderlyingTreeMaker();

        JCModifiers modifiers = treeMaker.Modifiers(Modifier.PUBLIC | Modifier.STATIC);
        List<JCTypeParameter> methodGenericTypes = List.nil();
        JCExpression methodType = treeMaker.TypeIdent(TypeTag.VOID);
        Name methodName = node.toName("testingYes");
        List<JCVariableDecl> methodParameters = List.nil();
        List<JCExpression> methodThrows = List.nil();

        JCExpression println = chainDots(node, "System", "out", "println");
        List<JCExpression> printlnArgs = List.of(treeMaker.Literal("test message"));
        JCMethodInvocation printlnInvocation = treeMaker.Apply(List.nil(), println, printlnArgs);
        JCBlock methodBody = treeMaker.Block(0, List.of(treeMaker.Exec(printlnInvocation)));

        return treeMaker.MethodDef(modifiers, methodName, methodType, methodGenericTypes, methodParameters, methodThrows, methodBody, null);
    }
}
