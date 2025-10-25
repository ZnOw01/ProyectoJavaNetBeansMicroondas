package lenguajemicroondas;

import java.io.*;

%%
%class MicroondasLexer
%public
%unicode
%line
%column
%type Tokens
%function yylex  // Esto es importante

%{
    // Código Java que se incluye en la clase del lexer
    public String getLexeme() {
        return yytext();
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

%%

<YYINITIAL> {
    // Tokens de comandos del microondas (case-insensitive)
    ("abrir"|"ABRIR")     { return Tokens.Abrir; }
    ("cerrar"|"CERRAR")   { return Tokens.Cerrar; }
    ("encender"|"ENCENDER") { return Tokens.Encender; }
    ("apagar"|"APAGAR")   { return Tokens.Apagar; }
    ("potencia"|"POTENCIA") { return Tokens.Potencia; }
    ("pausar"|"PAUSAR")   { return Tokens.Pausar; }
    ("cocinar"|"COCINAR") { return Tokens.Cocinar; }
    ("tiempo"|"TIEMPO")   { return Tokens.Tiempo; }
    
    // Espacios en blanco - ignorar
    {WhiteSpace}          { /* ignore */ }
}

// Carácter no reconocido
[^] { return Tokens.ERROR; }