# Cambios Implementados: Nuevos Comandos

## Resumen
Se han agregado los siguientes comandos al lenguaje del microondas:

- **INICIO**: Comando obligatorio que debe aparecer al principio de todo programa
- **FINAL**: Comando obligatorio que debe aparecer al final de todo programa
- **REANUDAR**: Comando para reanudar la cocción después de una pausa

## Nueva Sintaxis

### Estructura Obligatoria
Todos los programas DEBEN seguir esta estructura:

```
INICIO
    <comandos del microondas>
FINAL
```

### Ejemplo Válido
```
INICIO
encender
potencia 5
tiempo 2:30
cocinar
pausar
reanudar
apagar
FINAL
```

### Ejemplo Inválido (sin INICIO/FINAL)
```
encender
potencia 5
cocinar
```
❌ Este programa será rechazado por el parser porque no tiene INICIO y FINAL

## Archivos Modificados

### 1. `src/Cup/Grammar.cup`
- Se agregaron los terminales: `Inicio`, `Final`, `Reanudar`
- Se modificó la regla `PROGRAMA` para requerir: `Inicio COMANDOS Final`
- Se agregó el no terminal `COMANDO_REANUDAR`

### 2. `src/Cup/entradaCup.jflex`
- Se agregaron reglas léxicas para reconocer:
  - `inicio` / `INICIO`
  - `final` / `FINAL`
  - `reanudar` / `REANUDAR`

### 3. `src/Cup/sym.java`
- Se agregaron las constantes:
  - `Inicio = 15`
  - `Final = 16`
  - `Reanudar = 17`

### 4. `src/Cup/LexerCup.java`
- **REGENERADO automáticamente** con JFlex
- Ahora reconoce los nuevos tokens

## Estado de la Implementación

### ✅ Completado
- Gramática actualizada (Grammar.cup)
- Especificación léxica actualizada (entradaCup.jflex)
- Lexer regenerado (LexerCup.java)
- Símbolos actualizados (sym.java)

### ⚠️ Pendiente
- **Parser (Syntactic.java)**: Necesita ser regenerado con CUP

## Cómo Regenerar el Parser

### Opción 1: Usando el script automatizado
```bash
# 1. Descargar las librerías de CUP
./scripts/fetch-libs.sh

# 2. Regenerar los analizadores
./scripts/regenerate-parser.sh
```

### Opción 2: Manualmente con NetBeans
1. Abrir el proyecto en NetBeans
2. Ejecutar: `LenguajeMicroondas.generarCup()`
3. Recompilar el proyecto

### Opción 3: Línea de comandos con las librerías
```bash
# Regenerar lexer
java -jar libs/JFlex.jar src/Cup/entradaCup.jflex

# Regenerar parser
java -jar libs/java-cup-11a.jar -parser Syntactic -destdir src/Cup src/Cup/Grammar.cup
```

## Comandos Disponibles

| Comando | Parámetros | Descripción | Ejemplo |
|---------|-----------|-------------|---------|
| `inicio` | - | Inicia un programa (obligatorio) | `INICIO` |
| `final` | - | Finaliza un programa (obligatorio) | `FINAL` |
| `abrir` | - | Abre la puerta | `abrir` |
| `cerrar` | - | Cierra la puerta | `cerrar` |
| `encender` | - | Enciende el microondas | `encender` |
| `apagar` | - | Apaga el microondas | `apagar` |
| `potencia` | número con +/- | Establece la potencia | `potencia 5`, `potencia 8+` |
| `tiempo` | número o mm:ss | Establece el tiempo | `tiempo 30`, `tiempo 2:30` |
| `cocinar` | - | Inicia la cocción | `cocinar` |
| `pausar` | - | Pausa la cocción | `pausar` |
| `reanudar` | - | Reanuda la cocción | `reanudar` |

## Notas Importantes

1. **Case-Insensitive**: Todos los comandos son insensibles a mayúsculas/minúsculas
   - `INICIO` = `inicio` = `InIcIo`

2. **INICIO y FINAL son obligatorios**: El parser rechazará cualquier programa que no comience con INICIO y termine con FINAL

3. **Comentarios**: Se pueden usar comentarios con `#`
   ```
   INICIO
   # Este es un comentario
   encender
   FINAL
   ```

4. **Whitespace**: Los espacios, tabs y saltos de línea son ignorados

## Próximos Pasos

1. Regenerar `Syntactic.java` con CUP cuando estén disponibles las librerías
2. Actualizar la interfaz gráfica para mostrar/validar INICIO/FINAL
3. Agregar validación semántica para asegurar INICIO/FINAL en el simulador
4. Actualizar los ejemplos en la interfaz de usuario
