package astwalker;

class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return "";
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return "";
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "";
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return "Not implemented yet"; // TODO: pendiente de implementar;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return "Not implemented yet"; // TODO: pendiente de implementar;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "Not implemented yet"; // TODO: pendiente de implementar
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "Not implemented yet!"; // TODO: pendiente de implementar!!!
    }

    private String parenthesize(String name, Expr... expressions) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(").append(name);

        for (Expr expr : expressions) {
            stringBuilder.append(" ");
            stringBuilder.append(expr.accept(this));
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
