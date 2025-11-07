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

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
DIGITO         = [0-9]
Numero         = {DIGITO}+
Comentario     = \# [^\r\n]*

%%

<YYINITIAL> {
    /* Comandos de control (case insensitive) */
    ("inicio"|"INICIO")       { return symbol(sym.Inicio, yytext()); }
    ("final"|"FINAL")         { return symbol(sym.Final, yytext()); }

    /* Comandos del microondas */
    ("abrir"|"ABRIR")         { return symbol(sym.Abrir, yytext()); }
    ("cerrar"|"CERRAR")       { return symbol(sym.Cerrar, yytext()); }
    ("encender"|"ENCENDER")   { return symbol(sym.Encender, yytext()); }
    ("apagar"|"APAGAR")       { return symbol(sym.Apagar, yytext()); }
    ("potencia"|"POTENCIA")   { return symbol(sym.Potencia, yytext()); }
    ("pausar"|"PAUSAR")       { return symbol(sym.Pausar, yytext()); }
    ("reanudar"|"REANUDAR")   { return symbol(sym.Reanudar, yytext()); }
    ("cocinar"|"COCINAR")     { return symbol(sym.Cocinar, yytext()); }
    ("tiempo"|"TIEMPO")       { return symbol(sym.Tiempo, yytext()); }

    {Numero}                  { return symbol(sym.Numero, Integer.valueOf(yytext())); }

    ":"                       { return symbol(sym.DOS_PUNTOS, yytext()); }
    "+"                       { return symbol(sym.MAS, yytext()); }
    "-"                       { return symbol(sym.MENOS, yytext()); }

    {Comentario}              { /* Ignorar */ }
    {WhiteSpace}              { /* Ignorar */ }
}

. { return symbol(sym.ERROR, yytext()); }
