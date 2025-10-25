package lenguajemicroondas;

/**
 *
 * @author guill
 */
public class TokenInfo {
    private Tokens tokenType;
    private String lexeme;
    private int line;
    private int column;
    
    public TokenInfo(Tokens tokenType, String lexeme, int line, int column) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }
    
    // Getters
    public Tokens getTokenType() { return tokenType; }
    public String getLexeme() { return lexeme; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    
    @Override
    public String toString() {
        return String.format("Token[%s, '%s'] en línea %d, columna %d", 
                           tokenType, lexeme, line, column);
    }
}
