package Cup;

import java_cup.runtime.Symbol;

%%
%class LexerCup
%type java_cup.runtime.Symbol
%cup
%full
%line
%char
%public
%unicode

%{
    private Symbol symbol(int type, Object value){
        return new Symbol(type, yyline, yycolumn, value);
    }
    private Symbol symbol(int type){
        return new Symbol(type, yyline, yycolumn);
    }
%}

/* ===== Definiciones ===== */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
DIGITO         = [0-9]
Numero         = {DIGITO}+
Comentario     = \# [^\r\n]*

%%

/* ===== Reglas ===== */
<YYINITIAL> {

    /* Palabras clave (insensible a mayúsculas usando dos variantes) */
    ("abrir"|"ABRIR")         { return symbol(sym.Abrir, yytext()); }
    ("cerrar"|"CERRAR")       { return symbol(sym.Cerrar, yytext()); }
    ("encender"|"ENCENDER")   { return symbol(sym.Encender, yytext()); }
    ("apagar"|"APAGAR")       { return symbol(sym.Apagar, yytext()); }
    ("potencia"|"POTENCIA")   { return symbol(sym.Potencia, yytext()); }
    ("pausar"|"PAUSAR")       { return symbol(sym.Pausar, yytext()); }
    ("cocinar"|"COCINAR")     { return symbol(sym.Cocinar, yytext()); }
    ("tiempo"|"TIEMPO")       { return symbol(sym.Tiempo, yytext()); }

    /* Números */
    {Numero}                  { return symbol(sym.Numero, Integer.valueOf(yytext())); }

    /* Símbolos */
    ":"                       { return symbol(sym.DOS_PUNTOS, yytext()); }
    "+"                       { return symbol(sym.MAS, yytext()); }
    "-"                       { return symbol(sym.MENOS, yytext()); }

    /* Comentarios y espacios (ignorar) */
    {Comentario}              { /* ignorar comentario de línea */ }
    {WhiteSpace}              { /* ignorar blancos */ }
}

/* Cualquier otro carácter → error (mantener al final) */
. { return symbol(sym.ERROR, yytext()); }
