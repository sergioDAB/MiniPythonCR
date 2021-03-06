package visitors;

import symbolTable.Scope;
import symbolTable.Table;
import symbolTable.TokenCR;
import com.textEditor.JCEditor;
import grammar.MPGrammarParser;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Created by anthony on 22/04/17.
 * Realiza el analisis semantico usando el patron de diseño Visitor
 */
public class SemanticVisitor extends BaseVisitorCR {

    private Table tablaSimbolos = new Table();
    //Indica si el proceso de analisis semantico se realizo con exito o no
    private Boolean Status = true;


    @Override
    public Object visitProgramN(MPGrammarParser.ProgramNContext ctx) {

        visit(ctx.statement(0));
        for(int i = 1; i <= ctx.statement().size() - 1; i++){
            visit(ctx.statement(i));
        }

        tablaSimbolos.scopeActual().imprimirScope();

        return null;
    }

    @Override
    public Object visitStatdef(MPGrammarParser.StatdefContext ctx){

        return visit(ctx.defStatement());
    }


    /**
     * Visit a parse tree produced by the {@code statif}
     * labeled alternative in {@link MPGrammarParser#statement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStatif(MPGrammarParser.StatifContext ctx){

        return visit(ctx.ifStatement());
    }


    /**
     * Visit a parse tree produced by the {@code statret}
     * labeled alternative in {@link MPGrammarParser#statement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStatret(MPGrammarParser.StatretContext ctx){

        return visit(ctx.returnStatement());
    }


    /**
     * Visit a parse tree produced by the {@code statprint}
     * labeled alternative in {@link MPGrammarParser#statement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStatprint(MPGrammarParser.StatprintContext ctx){

        return visit(ctx.printStatement());
    }


    /**
     * Visit a parse tree produced by the {@code statwhile}
     * labeled alternative in {@link MPGrammarParser#statement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStatwhile(MPGrammarParser.StatwhileContext ctx){

        return visit(ctx.whileStatement());
    }


    /**
     * Visit a parse tree produced by the {@code statassign}
     * labeled alternative in {@link MPGrammarParser#statement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStatassign(MPGrammarParser.StatassignContext ctx){

        return visit(ctx.assignStatement());
    }


    /**
     * Visit a parse tree produced by the {@code statfncall}
     * labeled alternative in {@link MPGrammarParser#statement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStatfncall(MPGrammarParser.StatfncallContext ctx){

        return visit(ctx.functionCallStatement());
    }


    /**
     * Obtiene el tipo de dato por cada parametro de la funcion
     * @param parametros
     * @return
     */
    private int [] getTipoParametros(Object [] parametros){
        int [] tipos = new int [parametros.length];

        //Se buscan los parametros en el scope de la funcion, en el momento en que se llama este metodo solo se
        //encuentran los parametros en la tabla de este scope
        for(int i = 0; i < parametros.length; i++){
            Scope.Identificador id = tablaSimbolos.scopeActual().buscar(parametros[i].toString());
            tipos[i] = id.getTipo();
        }

        return tipos;
    }

    /**
     * Visit a parse tree produced by the {@code defstat}
     * labeled alternative in {@link MPGrammarParser#defStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitDefstat(MPGrammarParser.DefstatContext ctx){

        tablaSimbolos.abrirScope("Def_Scope_" + ctx.IDENTIFIER().getText());
        //Busca los parametros de la funcion y revisa que esten declarados de manera global
        Object [] params = (Object[]) visit(ctx.argList());
        //Busca el tipo de dato por cada parametro
        int [] tiposParams = getTipoParametros(params);
        //Busca el tipo de retorno de la funcion
        Object ret = visit(ctx.sequence());
        int tipoRetorno = (ret == null) ? Table.NULL : (int)ret;

        tablaSimbolos.scopeActual().imprimirScope();

        tablaSimbolos.cerrarScope();

        //Se agrega la funcion al scope actual
        tablaSimbolos.scopeActual().insertarFuncion(ctx.IDENTIFIER().getSymbol(), ctx, params, tiposParams, tipoRetorno);

        return null;
    }


    /**
     * Visit a parse tree produced by the {@code morearglist}
     * labeled alternative in {@link MPGrammarParser#argList}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMorearglist(MPGrammarParser.MorearglistContext ctx){

        Scope scopeActual = tablaSimbolos.scopeActual();
        //Se busca el identificador en la tabla
        Scope.Identificador identificador = tablaSimbolos.buscar(ctx.IDENTIFIER().getText());
        if(identificador != null){
            String nombre = identificador.getNombre();
            int tipo = identificador.getTipo();
            ParserRuleContext cntx = identificador.getDecl();
            scopeActual.insertar(new CommonToken(tipo, nombre), ctx);
        }
        else {
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "Error no se encuentra los tipos de los parametros";
            JCEditor.showMessage(tmp);

            return null;
        }

        Object [] params = (Object[]) visit(ctx.moreArgs());
        params[0] = ctx.IDENTIFIER().getText();

        return params;
    }


    /**
     * Visit a parse tree produced by the {@code epsarglist}
     * labeled alternative in {@link MPGrammarParser#argList}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitEpsarglist(MPGrammarParser.EpsarglistContext ctx){

        Object [] params = new Object[0];

        return params;
    }


    /**
     * Visit a parse tree produced by the {@code moreargsN}
     * labeled alternative in {@link MPGrammarParser#moreArgs}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMoreargsN(MPGrammarParser.MoreargsNContext ctx){
        Scope scopeActual = tablaSimbolos.scopeActual();
        Object [] params = new Object[ctx.IDENTIFIER().size() + 1];
        int i = 1;
        //Se agregan los argumentos al scope de la funcion
        for(TerminalNode node : ctx.IDENTIFIER()){
            Scope.Identificador identificador = tablaSimbolos.buscar(node.getText());
            if(identificador != null){
                String nombre = identificador.getNombre();
                int tipo = identificador.getTipo();
                ParserRuleContext cntx = identificador.getDecl();
                scopeActual.insertar(new CommonToken(tipo, nombre), ctx);
            }
            else {
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error no se encuentra los tipos de los parametros";
                JCEditor.showMessage(tmp);

                return null;
            }
            params[i] = node.getText();
            i++;
        }

        return params;
    }


    /**
     * Visit a parse tree produced by the {@code ifstat}
     * labeled alternative in {@link MPGrammarParser#ifStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitIfstat(MPGrammarParser.IfstatContext ctx){

        tablaSimbolos.abrirScope("If_Scope");
        //Se comprueba si la condición es correcta
        Object cmprsn = visit(ctx.expression());
        if(cmprsn == null){
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "Condición invalida en IF ";
            JCEditor.showMessage(tmp);

        }
        visit(ctx.sequence(0));
        visit(ctx.sequence(1));

        tablaSimbolos.scopeActual().imprimirScope();

        tablaSimbolos.cerrarScope();

        return null;
    }


    /**
     * Comprueba si las variables usadas estan declaradas y si se pueden comparar
     * Visit a parse tree produced by the {@code whilestat}
     * labeled alternative in {@link MPGrammarParser#whileStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitWhilestat(MPGrammarParser.WhilestatContext ctx){

        tablaSimbolos.abrirScope("While_Scope");

        visit(ctx.expression());
        visit(ctx.sequence());

        tablaSimbolos.scopeActual().imprimirScope();

        tablaSimbolos.cerrarScope();

        return null;
    }


    /**
     * Visit a parse tree produced by the {@code returnstat}
     * labeled alternative in {@link MPGrammarParser#returnStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitReturnstat(MPGrammarParser.ReturnstatContext ctx){

        return visit(ctx.expression());
    }


    /**
     * Visit a parse tree produced by the {@code printstat}
     * labeled alternative in {@link MPGrammarParser#printStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitPrintstat(MPGrammarParser.PrintstatContext ctx){

        Object exprsn = visit(ctx.expression());
        if(exprsn == null){
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "Error en el print";
            JCEditor.showMessage(tmp);

        }

        return null;
    }


    /**
     * Visit a parse tree produced by the {@code assignstat}
     * labeled alternative in {@link MPGrammarParser#assignStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitAssignstat(MPGrammarParser.AssignstatContext ctx) {

        //ID = expression
        Object tipo = visit(ctx.expression());

        if (tipo != null) {

            if (tipo.getClass().toString().equals("class symbolTable.TokenCR")) {
                //TODO comprobar si ya esta declarado
                ((TokenCR) tipo).setNombre(ctx.IDENTIFIER().getText());
                tablaSimbolos.scopeActual().insertarLista((TokenCR) tipo, ctx, ((TokenCR) tipo).getLista());
                return null;

            }
            if ((int) tipo != 0) {

                String id = ctx.IDENTIFIER().getText();
                //Se busca el id en la tabla de simbolos
                Scope.Identificador identificador = tablaSimbolos.scopeActual().buscar(id);
                if (identificador == null) {
                    tablaSimbolos.scopeActual().insertar(new CommonToken((int) tipo, id), ctx);
                }
                //Se comprueba si los tipos coinciden
                else {
                    if (identificador.getTipo() != (int) tipo) {
                        String tmp = JCEditor.consoleTextArea.getText() + "\n";
                        tmp += "Error tipos incompatibles en línea " + ctx.IDENTIFIER().getSymbol().getLine();
                        JCEditor.showMessage(tmp);

                    }
                }
            }
        }

        return null;
    }

    private boolean compareArgs(int [] funcArgs, Object[] callArgs){
        boolean iguales = true;

        if(funcArgs.length != callArgs.length){
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "Numero invalido de parametros, Se esperaban : " + funcArgs.length +
                    " Recibidos : " + callArgs.length;
            JCEditor.showMessage(tmp);

            return false;
        }

        for(int i = 0; i < funcArgs.length; i++){
            if(funcArgs[i] != (int)callArgs[i]){
                iguales = false;
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Tipo de parametro invalido, Se esperaba : " +
                        Table._SYMBOLIC_NAMES[funcArgs[i]] + " Recibido : " + Table._SYMBOLIC_NAMES[(int)callArgs[i]];
                JCEditor.showMessage(tmp);

            }
        }

        return iguales;
    }

    /**Esta regla aplica para llamadas simples de funciones como funcion()
     * Visit a parse tree produced by the {@code fncallstat}
     * labeled alternative in {@link MPGrammarParser#functionCallStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitFncallstat(MPGrammarParser.FncallstatContext ctx){

        //Busca el identificador de la funcion y verifica si esta declarada
        Object token = visit(ctx.primitiveExpression());
        if(token != null){
            //identificador de la funcion
            token = (Token)token;
            Scope.Identificador funcion = tablaSimbolos.buscar(((Token) token).getText());
            int [] params = funcion.getTipoParametros();
            Object[] args = (Object[])visit(ctx.expressionList());
            if(!compareArgs(params, args)){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Parametros invalidos en la llamada a la funcion " + ctx.getText() +
                        " Línea " + ctx.PDER().getSymbol().getLine();
                JCEditor.showMessage(tmp);

            }
        }

        return null;
    }


    /**
     * Visit a parse tree produced by the {@code seq}
     * labeled alternative in {@link MPGrammarParser#sequence}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitSeq(MPGrammarParser.SeqContext ctx){

        return visit(ctx.moreStatement());
    }


    /**
     * Visit a parse tree produced by the {@code morestats}
     * labeled alternative in {@link MPGrammarParser#moreStatement}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMorestats(MPGrammarParser.MorestatsContext ctx){

        Object tmp = visit(ctx.statement(0));
        for(int i = 1; i <= ctx.statement().size() - 1; i++){
            tmp = visit(ctx.statement(i));
        }
        //return visit(ctx.statement(ctx.statement().size() - 1));
        return tmp;
    }


    /**
     * Visit a parse tree produced by the {@code exprsn}
     * labeled alternative in {@link MPGrammarParser#expression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitExprsn(MPGrammarParser.ExprsnContext ctx){

        Object result = null;

        Object exp = visit(ctx.additionExpression());
        Object cmprsn = visit(ctx.comparison());

        //Si existe una comparación
        if(cmprsn != null){
            //Se revisa si los tipos son comparables
            //Solo se pueden comparar int o char
            if((int)cmprsn == Table.ERROR){
                return null;
            }

            if((int)cmprsn != MPGrammarParser.INTEGER && (int)cmprsn != MPGrammarParser.CHAR && (int)exp != MPGrammarParser.INTEGER && (int)exp != MPGrammarParser.CHAR){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error solo se pueden comparar INTEGER y CHAR";
                JCEditor.showMessage(tmp);

            }

            else if((int)cmprsn != (int)exp){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error tipos incompatibles";
                JCEditor.showMessage(tmp);

            }

            //Todo esta bien
            else {
                result = Table.BOOL;
            }
        }
        else {
            result = exp;
        }

        return result;
    }


    /**
     * Chequea si la instruccion es valida y retorna el tipo de los elementos comparados
     * Visit a parse tree produced by the {@code cmparison}
     * labeled alternative in {@link MPGrammarParser#comparison}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitCmparison(MPGrammarParser.CmparisonContext ctx){

        //Se comprueba el tipo de los elementos comparados, si no son iguales no es valido

        Object tipo = null;
        if(ctx.additionExpression().size() == 1){
            tipo = visit(ctx.additionExpression(0));
            if(tipo == null) tipo = Table.ERROR;
        }
        else if(ctx.additionExpression().size() > 1){
            int tipoAnt = (int)visit(ctx.additionExpression(0));
            for(int i = 0; i < ctx.additionExpression().size(); i++){
                if(tipoAnt != (int)visit(ctx.additionExpression(i))){
                    String tmp = JCEditor.consoleTextArea.getText() + "\n";
                    tmp += "Error tipos compatibles en comparación";
                    JCEditor.showMessage(tmp);
                    tipo = Table.ERROR;
                    break;
                }
            }
        }
        return tipo;
    }


    /**
     * Visit a parse tree produced by the {@code addexp}
     * labeled alternative in {@link MPGrammarParser#additionExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitAddexp(MPGrammarParser.AddexpContext ctx){

        Object result = null;
        Object mulexp = visit(ctx.multiplicationExpression());
        Object addFact = visit(ctx.additionFactor());

        if(mulexp.getClass().toString().equals("class symbolTable.TokenCR")){
            return mulexp;
        }

        if(addFact != null){
            //Error en la expresion
            if((int)addFact == Table.ERROR){
                return null;
            }
            if((int)mulexp != (int)addFact){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error elementos incompatibles " +
                        " " + Table._SYMBOLIC_NAMES[(int)mulexp] + " [+ , -] " + Table._SYMBOLIC_NAMES[(int)addFact] +
                        " en " + "'" + ctx.getText() + "'";
                JCEditor.showMessage(tmp);

            }
            else if((int)mulexp != MPGrammarParser.INTEGER && (int) mulexp != MPGrammarParser.STRING && (int)addFact != MPGrammarParser.INTEGER && (int) addFact != MPGrammarParser.STRING){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error tipos de datos invalidos para la suma";
                JCEditor.showMessage(tmp);

            }
            else {
                result = mulexp;
            }
        }
        else {
            result = mulexp;
        }

        return result;
    }


    /**
     * Visit a parse tree produced by the {@code addfact}
     * labeled alternative in {@link MPGrammarParser#additionFactor}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitAddfact(MPGrammarParser.AddfactContext ctx){

        Object tipo = null;

        if(ctx.multiplicationExpression().size() == 1){
            tipo = visit(ctx.multiplicationExpression(0));
        }
        else if(ctx.multiplicationExpression().size() > 1){
            int tipoAnt = (int)visit(ctx.multiplicationExpression(0));
            for(int i = 0; i < ctx.multiplicationExpression().size(); i++){
                if(tipoAnt != (int)visit(ctx.multiplicationExpression(i))){
                    String tmp = JCEditor.consoleTextArea.getText() + "\n";
                    tmp += "Error tipos incompatibles";
                    JCEditor.showMessage(tmp);
                    tipo = Table.ERROR;
                    break;
                }
            }
        }

        return tipo;
    }


    /**
     * Realiza el chequeo de tipos y retorna el tipo de la expresion
     * Visit a parse tree produced by the {@code mulexp}
     * labeled alternative in {@link MPGrammarParser#multiplicationExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMulexp(MPGrammarParser.MulexpContext ctx){

        Object result = null;
        Token elmnt1 = (Token) visit(ctx.elementExpression());
        Object elmnt2 = visit(ctx.multiplicationFactor());

        if(elmnt1.getClass().toString().equals("class symbolTable.TokenCR")){
            return elmnt1;
        }

        if(elmnt2 != null){
            //Error en la expresion
            if((int)elmnt2 == Table.ERROR){
                return null;
            }
            if(elmnt1.getType() != (int)elmnt2 ){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error elementos incompatibles en la multiplicacion en línea " + elmnt1.getLine() +
                        " " + Table._SYMBOLIC_NAMES[elmnt1.getType()] + " [* , /] " + Table._SYMBOLIC_NAMES[(int)elmnt2] +
                        " en " + "'" + ctx.getText() + "'";
                JCEditor.showMessage(tmp);

            }
            else if(elmnt1.getType() != MPGrammarParser.INTEGER && (int)elmnt2 != MPGrammarParser.INTEGER){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error tipos de datos invalidos para la multiplicación en línea " + elmnt1.getLine();
                JCEditor.showMessage(tmp);

            }
            else {
                result = elmnt1.getType();
            }
        }
        else{
            result = elmnt1.getType();
        }

        return result;
    }


    /**
     * Visit a parse tree produced by the {@code mulfact}
     * labeled alternative in {@link MPGrammarParser#multiplicationFactor}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMulfact(MPGrammarParser.MulfactContext ctx){

        Object tipo = null;

        if(ctx.elementExpression().size() == 1){
            tipo = ((Token)visit(ctx.elementExpression(0))).getType();
        }

        else if(ctx.elementExpression().size() > 1){
            int tipoAnt = ((Token)visit(ctx.elementExpression(0))).getType();
            for(int i = 0; i < ctx.elementExpression().size(); i++) {
                if(tipoAnt != ((Token)visit(ctx.elementExpression(i))).getType()){
                    String tmp = JCEditor.consoleTextArea.getText() + "\n";
                    tmp += "Error tipos incompatibles";
                    JCEditor.showMessage(tmp);
                    tipo = Table.ERROR;
                    break;
                }
            }
        }

        return tipo;
    }


    /**
     * Visit a parse tree produced by the {@code elmntexp}
     * labeled alternative in {@link MPGrammarParser#elementExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitElmntexp(MPGrammarParser.ElmntexpContext ctx){

        Object prmt = visit(ctx.primitiveExpression());
        Object result = prmt;
        Object elmntacss = visit(ctx.elementAccess());

        if(elmntacss != null){
            Scope.Identificador id = tablaSimbolos.buscar(((Token)prmt).getText());
            System.out.println(id.isEsLista());
            if(id.isEsLista()){
                int index = Integer.parseInt(((Token)elmntacss).getText());
                int valor = id.getIndex(index);
                if(valor != -1){
                    result = new CommonToken(valor, Table._SYMBOLIC_NAMES[valor]);
                }
                else {
                    result = new CommonToken(0, "Null");
                }
            }
            else{
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Error " + Table._SYMBOLIC_NAMES[id.getTipo()] + " no puede ser accesado -> "+
                        ctx.getText();
                JCEditor.showMessage(tmp);

                result = new CommonToken(0, "Null");
            }
        }

        return result;
    }


    //Solucion poco elegante
    private Boolean elmntAcss = false;

    /**
     * Visit a parse tree produced by the {@code elmntacess}
     * labeled alternative in {@link MPGrammarParser#elementAccess}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitElmntacess(MPGrammarParser.ElmntacessContext ctx){

        boolean error = false;
        Token result = null;

        //Se chequean las expresiones
        for (int i = 0; i < ctx.expression().size(); i++){
            if(visit(ctx.expression(i)) == null){
                error = true;
            }
        }

        //Cochinada para que funcione
        if(!ctx.expression().isEmpty()){
            elmntAcss = true;
            Object l = visit(ctx.expression(0));
            String value = ((Token)l).getText();
            result = (error)? null : new CommonToken((int)visit(ctx.expression(ctx.expression().size() - 1)), value);
        }

        return result;
    }


    /**
     * Visit a parse tree produced by the {@code fncallexp}
     * labeled alternative in {@link MPGrammarParser#functionCallExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    public Object visitFncallexp(MPGrammarParser.FncallexpContext ctx){

        //Retorna el tipo de dato que devuelve la funcion
        Object funcType = null;
        //Primero se verifica que la funcion esta declarada
        Scope.Identificador funcion = tablaSimbolos.buscar(ctx.IDENTIFIER().getText());
        if(funcion != null){
            int [] params = funcion.getTipoParametros();
            Object [] args = (Object[])visit(ctx.expressionList());
            if(!compareArgs(params, args)){
                String tmp = JCEditor.consoleTextArea.getText() + "\n";
                tmp += "Parametros invalidos en la llamada a la funcion " + ctx.getText() +
                        " Línea " + ctx.PDER().getSymbol().getLine();
                JCEditor.showMessage(tmp);

            }
            else {
                funcType = new CommonToken(funcion.getTipo(), ctx.IDENTIFIER().getText());
            }
        }
        else {
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "La funcion " + ctx.IDENTIFIER().getText() + " no esta declarada " +
                    "Línea " + ctx.IDENTIFIER().getSymbol().getLine();
            JCEditor.showMessage(tmp);

        }


        return funcType;
    }


    /**Obtiene los parametros de las llamadas a funcion
     * Visit a parse tree produced by the {@code moreexplist}
     * labeled alternative in {@link MPGrammarParser#expressionList}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMoreexplist(MPGrammarParser.MoreexplistContext ctx){

        Object elmnt1 = visit(ctx.expression());
        Object [] elmnts = (Object[]) visit(ctx.moreExpressions());
        elmnts[0] = elmnt1;

        return elmnts;
    }


    /**
     * Visit a parse tree produced by the {@code epsexplist}
     * labeled alternative in {@link MPGrammarParser#expressionList}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitEpsexplist(MPGrammarParser.EpsexplistContext ctx){

        Object [] elmnts = new Object[0];

        return elmnts;
    }


    /**
     * Visit a parse tree produced by the {@code moreexps}
     * labeled alternative in {@link MPGrammarParser#moreExpressions}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitMoreexps(MPGrammarParser.MoreexpsContext ctx){

        Object [] elmnts = new Object[ctx.expression().size() + 1];
        int index = 1;

        for(int i = 0; i < ctx.expression().size(); i++){
            elmnts[index] = visit(ctx.expression(i));
            index++;
        }

        return elmnts;
    }


    /**
     * Visit a parse tree produced by the {@code intexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse treedd
     * @return the visitor result
     */
    @Override
    public Object visitIntexp(MPGrammarParser.IntexpContext ctx){

        Token token = (!elmntAcss)? new CommonToken(MPGrammarParser.INTEGER, ctx.INTEGER().getText()):
                new TokenCR(MPGrammarParser.INTEGER, ctx.INTEGER().getText());

        if(elmntAcss) elmntAcss = false;

        return token;
    }


    /**
     * Visit a parse tree produced by the {@code strexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitStrexp(MPGrammarParser.StrexpContext ctx){

        Token token = new CommonToken(MPGrammarParser.STRING, ctx.STRING().getText());

        return token;
    }


    /**
     * Visit a parse tree produced by the {@code idexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitIdexp(MPGrammarParser.IdexpContext ctx){

        Token token = null;
        Scope.Identificador id = tablaSimbolos.buscar(ctx.IDENTIFIER().getText());
        if(id == null){
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "Error " + ctx.IDENTIFIER().getText() + " no esta declarado en este scope";
            JCEditor.showMessage(tmp);

        }
        else {
            token = new CommonToken(id.getTipo(), ctx.IDENTIFIER().getText());
        }

        return token;
    }


    /**
     * Visit a parse tree produced by the {@code chaexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitChaexp(MPGrammarParser.ChaexpContext ctx){
        Token token = new CommonToken(MPGrammarParser.CHAR, ctx.CHAR().getText());

        return token;
    }


    /**
     * Visit a parse tree produced by the {@code pizqexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitPizqexp(MPGrammarParser.PizqexpContext ctx){

        int resultado = (int)visit(ctx.expression());
        Token token = new CommonToken(resultado, "()");

        return token;
    }


    /**
     * Visit a parse tree produced by the {@code primlistexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitPrimlistexp(MPGrammarParser.PrimlistexpContext ctx){
        return visit(ctx.listExpression());
    }


    /**
     * Visit a parse tree produced by the {@code lenexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitLenexp(MPGrammarParser.LenexpContext ctx){

        Token result = null;

        int lista = (int) visit(ctx.expression());

        if(lista != Table.LISTA && lista != MPGrammarParser.STRING){
            String tmp = JCEditor.consoleTextArea.getText() + "\n";
            tmp += "Error len solo se puede aplicar a una lista o string" + ctx.getText();
            JCEditor.showMessage(tmp);

        }
        else{
            result = new CommonToken(MPGrammarParser.INTEGER, "len");
        }

        return result;
    }


    /**
     * Visit a parse tree produced by the {@code primfncallexp}
     * labeled alternative in {@link MPGrammarParser#primitiveExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitPrimfncallexp(MPGrammarParser.PrimfncallexpContext ctx){

        return visit(ctx.functionCallExpression());

    }


    /**
     * Visit a parse tree produced by {@link MPGrammarParser#listExpression}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitListExpression(MPGrammarParser.ListExpressionContext ctx){

        Object [] exprsnLst = (Object[]) visit(ctx.expressionList());
        Scope scopeActual = tablaSimbolos.scopeActual();

        Token token = new TokenCR(Table.LISTA, "Lista", exprsnLst);

        return token;
    }

}
