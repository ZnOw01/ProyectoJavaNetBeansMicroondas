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

%{
    private Symbol symbol(int type, Object value){
        return new Symbol(type, yyline, yycolumn, value);
    }
    
    private Symbol symbol(int type){
        return new Symbol(type, yyline, yycolumn);
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Numero         = [0-9]+

%%

/* Comandos del microondas (case-insensitive) */
<YYINITIAL> {
    ("abrir"|"ABRIR")     { return symbol(sym.Abrir, yytext()); }
    ("cerrar"|"CERRAR")   { return symbol(sym.Cerrar, yytext()); }
    ("encender"|"ENCENDER") { return symbol(sym.Encender, yytext()); }
    ("apagar"|"APAGAR")   { return symbol(sym.Apagar, yytext()); }
    ("potencia"|"POTENCIA") { return symbol(sym.Potencia, yytext()); }
    ("pausar"|"PAUSAR")   { return symbol(sym.Pausar, yytext()); }
    ("cocinar"|"COCINAR") { return symbol(sym.Cocinar, yytext()); }
    ("tiempo"|"TIEMPO")   { return symbol(sym.Tiempo, yytext()); }
    
    /* Números */
    {Numero}              { return symbol(sym.Numero, Integer.valueOf(yytext())); }
    
    /* Símbolos especiales */
    ":"                   { return symbol(sym.DOS_PUNTOS, yytext()); }
    "+"                   { return symbol(sym.MAS, yytext()); }
    "-"                   { return symbol(sym.MENOS, yytext()); }
    
    /* Espacios en blanco */
    {WhiteSpace}          { /* Ignorar */ }
}

/* Error de análisis */
. { return symbol(sym.ERROR, yytext()); }