package Team9.Scala;

import static Team9.Scala.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    //parser
    
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }
    
    
    
    // Declaration of Variable or a Function
    
    private Stmt declaration() {
        try {
            if (match(DEFINATION)) return function("function");
            if (match(VAR)) return varDeclaration();
            
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    
    // Variable Declaration
    
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        //consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }
    
    
    
    // Expression -> Assignment
    
    private Expr expression() {
        return assignment();
    }
    
    // Assignment -> or ( = ) Expression

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }
    
    // Or -> and

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    // and -> equality
    
    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    // equality -> comparison
    
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    
    // comparison -> addition()

    private Expr comparison() {
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // addition -> multiplication
    
    private Expr addition() {
        Expr expr = multiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    
    // multiplication -> unary

    private Expr multiplication() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary (!= || ==) call
    
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        
        
        
        return call();
    }
    
    // call -> primary

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }
    
    
    // Finish Call of the Function

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 8) {
                    error(peek(), "Cannot have more than 8 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }
    
   // Primary : Literals

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
          }
        
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }
    
    // Type Checking

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }
    
    // Consume token after evaluation

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }
    
    // Checking whether next is a Valid Token Type.

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peek().type == tokenType;
    }
    
    // Moving forward in the Token List.

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    // Checking for the end of the file.
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // Look into the next token
    private Token peek() {
        return tokens.get(current);
    }

    // look into previous token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // Sending the error message
    private ParseError error(Token token, String message) {
        Scala.error(token, message);
        return new ParseError();
    }

    // Checking for certain type of keywords.
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINTLINE:
                case RETURN:
                return;
            }

            advance();
        }
    }
	   
    
    
    
    // STATEMENTS
    
    
    

    
    // Selecting a Statement
    
    private Stmt statement() {
        //if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINTLINE)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }
    
    
    // Print Statement
    
    
    private Stmt printStatement() {
        Expr value = expression();
        //consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }
    
    // Expression Statement
    
    private Stmt expressionStatement() {
        Expr expr = expression();
        //consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
    
    
    // Block Statement
    
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    
 
    // If Statement
    
    
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    
    
    
    
    // While Statement
    
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }
    
    // For Statement
    // Different than LOX Language
    
    
    

    // Function Declaration
    
    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 8) {
                    error(peek(), "Cannot have more than 8 parameters.");
                }

                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }
    
    
    //Return Statement

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        //consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }
}






















//private Stmt forStatement() {
//    consume(LEFT_PAREN, "Expect '(' after 'for'.");
//
//    Stmt initializer;
//    if (match(SEMICOLON)) {
//        initializer = null;
//    } else if (match(VAR)) {
//        initializer = varDeclaration();
//    } else {
//        initializer = expressionStatement();
//    }
//
//    Expr condition = null;
//    if (!check(SEMICOLON)) {
//        condition = expression();
//    }
//    consume(SEMICOLON, "Expect ';' after loop condition.");
//
//    Expr increment = null;
//    if (!check(RIGHT_PAREN)) {
//        increment = expression();
//    }

	





//    consume(RIGHT_PAREN, "Expect ')' after for clauses.");
//
//    Stmt body = statement();
//
//    if (increment != null) {
//        body = new Stmt.Block(Arrays.asList(
//            body,
//            new Stmt.Expression(increment)));
//    }
//
//    if (condition == null) condition = new Expr.Literal(true);
//    body = new Stmt.While(condition, body);
//
//    if (initializer != null) {
//        body = new Stmt.Block(Arrays.asList(initializer, body));
//    }
//
//    return body;
//}
