package Team9.Scala;

import java.util.List;

public abstract class Expr {
	interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitSetExpr(Set expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
        R visitThisExpr(This expr);
    }
	
	// Literal
	
	static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }
	
	
	// Grouping
	
	static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }
	
	// Unary
	
	static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }
	
	
	//Binary
	
	
	static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }
	
	
	// Logical
	
	 static class Logical extends Expr {
	        Logical(Expr left, Token operator, Expr right) {
	            this.left = left;
	            this.operator = operator;
	            this.right = right;
	        }

	        <R> R accept(Visitor<R> visitor) {
	            return visitor.visitLogicalExpr(this);
	        }

	        final Expr left;
	        final Token operator;
	        final Expr right;
	    }
	
	
	// Assignment
	
	static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Token name;
        final Expr value;
    }
	
	
	
	// Variable
	
	static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        final Token name;
    }
	
	
	
	// Function Call
	
	 static class Call extends Expr {
	        Call(Expr callee, Token paren, List<Expr> arguments) {
	            this.callee = callee;
	            this.paren = paren;
	            this.arguments = arguments;
	        }

	        <R> R accept(Visitor<R> visitor) {
	            return visitor.visitCallExpr(this);
	        }

	        final Expr callee;
	        final Token paren;
	        final List<Expr> arguments;
	    }
	 
	 // Get the value;
	 
	 static class Get extends Expr {
	        Get(Expr object, Token name) {
	            this.object = object;
	            this.name = name;
	        }

	        <R> R accept(Visitor<R> visitor) {
	            return visitor.visitGetExpr(this);
	        }

	        final Expr object;
	        final Token name;
	    }

	 
	 // Set the value
	 
	 static class Set extends Expr {
	        Set(Expr object, Token name, Expr value) {
	            this.object = object;
	            this.name = name;
	            this.value = value;
	        }

	        <R> R accept(Visitor<R> visitor) {
	            return visitor.visitSetExpr(this);
	        }

	        final Expr object;
	        final Token name;
	        final Expr value;
	    }
	 
	 // This keyword to distinguish the variables.
	 
	 static class This extends Expr {
	        This(Token keyword) {
	            this.keyword = keyword;
	        }

	        <R> R accept(Visitor<R> visitor) {
	            return visitor.visitThisExpr(this);
	        }

	        final Token keyword;
	    }
	 
	 
	 
	 
	 abstract <R> R accept(Visitor<R> visitor);
}	
