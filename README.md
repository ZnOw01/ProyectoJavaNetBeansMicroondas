# Proyecto Lenguaje Microondas

Este repositorio contiene una aplicación de escritorio (Java Swing) que permite
analizar y simular un pequeño lenguaje específico de dominio pensado para
describir secuencias de uso de un microondas. El proyecto se apoya en un
**analizador léxico** generado con [JFlex](https://www.jflex.de/) y en un
**analizador sintáctico** generado con [Java CUP](http://www2.cs.tum.edu/projects/cup/),
ambos integrados dentro de una interfaz gráfica creada con NetBeans.

El README está organizado para documentar **cada archivo** del proyecto, explicar
su responsabilidad, cómo se relaciona con los demás componentes y por qué se
elige cada tecnología.

## Resumen de la arquitectura

- `lenguajemicroondas.LenguajeMicroondas` es el punto de entrada. Arranca la
  interfaz principal (`Ventana`) y ofrece utilidades para regenerar analizadores.
- `Ventana` y `VentanaSimulador` son las pantallas Swing que interactúan con los
  analizadores léxico/sintáctico y muestran los resultados al usuario.
- El directorio `src/Cup` agrupa la especificación del lenguaje (archivos `.cup`
  y `.jflex`) y los artefactos generados (lexer, parser y tabla de símbolos).
- El árbol `nbproject/` y `build.xml` contienen la configuración de NetBeans y
  de Apache Ant, encargada de automatizar la compilación y la regeneración de
  los analizadores.
- Los scripts en `scripts/` automatizan la descarga de dependencias y la
  regeneración de los analizadores fuera del IDE.

La siguiente sección describe directorio por directorio y archivo por archivo.

## Archivos en la raíz del repositorio

| Archivo | Descripción detallada |
|---------|-----------------------|
| `README.md` | Este documento. Resume la arquitectura, explica cada archivo, los flujos de trabajo y las decisiones de diseño. |
| `CAMBIOS_NUEVOS_COMANDOS.md` | Bitácora funcional. Describe la incorporación de los comandos `INICIO`, `FINAL` y `REANUDAR`, incluyendo la motivación, ejemplos de uso y los archivos que deben regenerarse tras los cambios. Sirve como guía histórica para entender por qué se ajustó la gramática. |
| `build.xml` | Script maestro de Apache Ant generado por NetBeans. Importa `nbproject/build-impl.xml` y, antes de compilar, ejecuta JFlex y CUP para regenerar `LexerCup.java`, `Syntactic.java` y `sym.java`. Conecta directamente con los jars esperados en `libs/`. |
| `manifest.mf` | Plantilla del manifiesto del JAR. NetBeans completará automáticamente la clase principal (`lenguajemicroondas.LenguajeMicroondas`) durante el empaquetado. |
| `libs/` | Carpeta donde se almacenan los jars externos (JFlex y Java CUP). Contiene un `README.md` con instrucciones y queda fuera del control de versiones el contenido pesado. |
| `scripts/` | Herramientas Bash. `fetch-libs.sh` descarga las dependencias oficiales desde Maven Central; `regenerate-parser.sh` ejecuta JFlex y CUP desde línea de comandos, útil cuando se modifica `entradaCup.jflex` o `Grammar.cup`. |
| `src/` | Código fuente del proyecto. Se subdivide en `Cup/` para la gramática y en `lenguajemicroondas/` para la interfaz y la lógica de la aplicación. |
| `nbproject/` | Configuración propia de NetBeans (propiedades del proyecto, rutas de fuentes, scripts de build generados automáticamente). |

## Directorio `libs/`

- `libs/README.md`: Explica por qué los jars no se versionan, qué archivos son
  necesarios (`JFlex.jar`, `java-cup-11a.jar`, `java_cup.jar`) y menciona el
  script `scripts/fetch-libs.sh` como método recomendado para descargarlos.

## Directorio `scripts/`

- `scripts/fetch-libs.sh`:
  - Bash script con `set -euo pipefail` para una ejecución segura.
  - Determina el directorio real de `libs/`, lo crea si no existe y descarga los
    jars oficiales mediante `curl`. Se evita descargar de nuevo archivos ya
    presentes, lo que agiliza flujos de trabajo repetitivos.
  - Se separó en función `download` para reutilizar la lógica de verificación y
    descarga de cada dependencia.
- `scripts/regenerate-parser.sh`:
  - Automatiza la regeneración del lexer (JFlex) y parser (CUP) sin depender del
    IDE.
  - Verifica que existan los jars necesarios antes de ejecutar los comandos,
    evitando errores confusos.
  - Ejecuta `java -jar libs/JFlex.jar src/Cup/entradaCup.jflex` y luego CUP con
    los parámetros adecuados (`-parser Syntactic -destdir src/Cup`).
  - Es la manera recomendada de sincronizar los artefactos generados cuando se
    cambian las reglas léxicas o sintácticas.

## Directorio `src/Cup/`

Este directorio encapsula la definición del lenguaje y los archivos generados.
El flujo típico es: editar `entradaCup.jflex` y/o `Grammar.cup`, ejecutar los
scripts para regenerar `LexerCup.java`, `Syntactic.java` y `sym.java`, y luego
compilar.

- `src/Cup/entradaCup.jflex`:
  - Especificación léxica en JFlex. Define tokens para cada comando (`inicio`,
    `final`, `abrir`, etc.), números, operadores `+`, `-`, separadores (`:`) y
    comentarios.
  - Las reglas son *case-insensitive* gracias a alternativas explícitas (`"ini"
    vs `"INI"`). Se retornan objetos `Symbol` de CUP con posición (`yyline`,
    `yycolumn`) para mejorar los reportes de error en la GUI.
  - Ignora espacios en blanco y comentarios iniciados con `#`, facilitando un
    lenguaje legible para humanos.
- `src/Cup/Grammar.cup`:
  - Gramática LR para Java CUP. Declara los terminales provenientes del lexer,
    define no terminales (`PROGRAMA`, `COMANDOS`, etc.) y establece que todo
    programa debe iniciar con `Inicio` y terminar con `Final`.
  - Contiene código embebido en el bloque `parser code` para capturar el último
    símbolo erróneo (`syntax_error`), información que la interfaz gráfica utiliza
    para señalar la ubicación del fallo al usuario.
  - El cuerpo de las producciones modela los comandos válidos del microondas y
    valida, por ejemplo, que la potencia acepte un número seguido opcionalmente
    de `+` o `-`, y que el tiempo pueda expresarse como segundos o `mm:ss`.
- `src/Cup/LexerCup.java`:
  - Artefacto generado por JFlex a partir de `entradaCup.jflex`. Implementa la
    interfaz `java_cup.runtime.Scanner`, traduce la entrada en tokens CUP y
    reporta posiciones de línea y columna.
  - No debe editarse a mano; cualquier cambio proviene de regenerar el lexer.
  - Se mantiene en el repositorio para facilitar la compilación sin requerir que
    cada usuario tenga JFlex instalado.
- `src/Cup/Syntactic.java`:
  - Parser generado por CUP. Consume los tokens emitidos por `LexerCup` y valida
    que la secuencia cumpla la gramática de `Grammar.cup`.
  - Incluye la misma lógica de captura de errores (`syntax_error`) definida en
    la gramática, exponiendo el símbolo conflictivo a través del método
    `getS()`. `Ventana` y `VentanaSimulador` utilizan este método para mostrar
    diagnósticos precisos.
  - **Nota importante**: el archivo actual refleja un parser generado antes de
    actualizar la producción principal a `Inicio ... Final`. El historial en
    `CAMBIOS_NUEVOS_COMANDOS.md` indica que se debe regenerar para que las
    producciones de la versión binaria coincidan con la gramática textual.
- `src/Cup/sym.java`:
  - Tabla de símbolos generada por CUP. Asigna un entero a cada token (`Inicio`,
    `Final`, `Reanudar`, etc.), lo que simplifica el `switch` en la interfaz y en
    el parser. Igual que los otros generados, se actualiza únicamente con CUP.

## Directorio `src/lenguajemicroondas/`

Código propio de la aplicación Java.

- `LenguajeMicroondas.java`:
  - Clase `final` con el método `main`. Usa `EventQueue.invokeLater` para crear
    la GUI en el hilo de eventos Swing (`Ventana`).
  - Ofrece utilidades estáticas `generarLexer()` y `generarCup()` para invocar
    JFlex y CUP desde el propio proyecto (útil en NetBeans). Maneja las rutas a
    `src/lenguajemicroondas` y `src/Cup`, comprueba existencia de archivos y
    mueve los artefactos generados a su carpeta destino.
  - La decisión de centralizar estas tareas evita duplicar lógica en scripts y
    permite regenerar analizadores desde la propia aplicación o el IDE.
- `Ventana.java`:
  - Ventana principal del analizador. Presenta tres columnas: entrada del usuario,
    resultado léxico y resultado sintáctico.
  - Al presionar “Analizar”, crea un `LexerCup` y un `Syntactic`, recorre los
    tokens para construir un reporte formateado e invoca `parser.parse()` para
    validar la gramática.
  - Implementa helpers para cargar/guardar archivos, preservar el último reporte,
    limpiar la interfaz y mostrar mensajes con `JOptionPane`. Su método
    `getTokenName` mapea los IDs de `sym` a nombres legibles.
  - Añade un `KeyListener` para invalidar reportes previos si el usuario modifica
    la entrada tras analizarla, garantizando que la UI siempre represente el
    estado real de la cadena analizada.
- `VentanaSimulador.java`:
  - Simulador interactivo del microondas. Combina botones físicos (abrir,
    encender, números, etc.) con un display, un panel animado y una consola de
    log.
  - Cada pulsación genera comandos en texto (`comandoParaParser`) que, al
    presionar “Cocinar”, se validan con el mismo lexer/parser que usa la ventana
    principal. Esto asegura que el simulador y el analizador comparten reglas.
  - Controla estados lógicos (puerta abierta, microondas encendido, modo de
    entrada de números), validaciones semánticas (p.ej. no cocinar con la puerta
    abierta) y gestiona un `Timer` Swing para contar el tiempo de cocción.
  - Muestra animaciones (`cooking.gif`, `stopped.png`) cuando están disponibles y
    registra cada acción en la consola para facilitar depuración.
  - Ejemplifica cómo integrar análisis sintáctico dentro de una lógica de
    negocio más rica.
- `Tokens.java` y `TokenInfo.java`:
  - Tipos utilitarios. `Tokens` enumera los tokens reconocidos y `TokenInfo`
    encapsula la terna `(token, lexema, posición)`. Aunque la GUI actual no usa
    directamente `TokenInfo`, queda disponible para extender la aplicación (por
    ejemplo, para resaltar tokens en un editor).
- `Ejemplo.txt`:
  - Archivo de ejemplos de programas válidos (y advertencias sobre casos
    inválidos). Sirve tanto como material de prueba manual como referencia para
    comprender el lenguaje.

## Configuración de NetBeans (`nbproject/`)

- `nbproject/project.properties`:
  - Propiedades de construcción: rutas de salida (`build`, `dist`), codificación,
    versión de Java objetivo, y el `classpath` que originalmente apuntaba a
    rutas locales (`E:\\UNT\\...`). Al usar `libs/` más los scripts, se
    reemplaza la necesidad de rutas absolutas.
  - Declara `main.class=lenguajemicroondas.LenguajeMicroondas`, por lo que el JAR
    generado arrancará la GUI.
- `nbproject/project.xml`:
  - Descripción de proyecto estándar de NetBeans J2SE. Define qué carpetas son
    fuentes y tests.
- `nbproject/build-impl.xml`:
  - Script generado automáticamente (no se edita a mano). Implementa los targets
    de Ant usados por NetBeans (`compile`, `run`, `clean`, etc.), extendidos por
    `build.xml`.
- `nbproject/genfiles.properties`:
  - Archivo de control para que NetBeans sepa cuándo regenerar `build-impl.xml`.
    No contiene lógica, pero su presencia evita que el IDE sobreescriba
    inadvertidamente scripts personalizados.

## Flujo de trabajo recomendado

1. **Descargar dependencias**: ejecutar `./scripts/fetch-libs.sh` para poblar la
   carpeta `libs/` con JFlex y CUP.
2. **Regenerar analizadores** si se modifican reglas léxicas o sintácticas:
   `./scripts/regenerate-parser.sh`. Alternativamente, llamar a
   `LenguajeMicroondas.generarLexer()` / `generarCup()` desde NetBeans.
3. **Compilar/Ejecutar** el proyecto con NetBeans o `ant run`. La GUI principal
   (`Ventana`) permite abrir un archivo, analizarlo y exportar un reporte; el
   simulador (`VentanaSimulador`) ofrece una experiencia interactiva.
4. **Validar cambios**: usar `src/lenguajemicroondas/Ejemplo.txt` como casos de
   prueba manual y observar la consola/logs para mensajes de error detallados.

## Decisiones de diseño

- **Separación de especificación y código generado**: mantener `.jflex` y `.cup`
  junto con los archivos generados permite compilar sin herramientas externas,
  pero los scripts facilitan sincronizarlos cuando cambian las reglas.
- **Uso de Swing**: se eligió Swing por su integración con NetBeans y porque el
  proyecto es principalmente educativo; no requiere dependencias externas ni
  frameworks modernos.
- **Validaciones enriquecidas** en `VentanaSimulador`: el simulador no solo
  confía en el parser, sino que añade lógica de negocio (puerta abierta, modo de
  entrada, timers). Esto demuestra cómo separar validaciones léxicas/sintácticas
  de reglas de negocio específicas.
- **Mensajes detallados de error**: tanto el lexer como el parser reportan
  posiciones; las ventanas convierten estos datos en mensajes amigables, lo cual
  es crucial en herramientas de análisis.

## Próximos pasos sugeridos

- Regenerar `Syntactic.java` para que las producciones internas coincidan con la
  gramática actual (`Inicio ... Final`).
- Integrar `VentanaSimulador` a la experiencia principal (por ejemplo, añadiendo
  un botón en `Ventana` que la abra) y compartir reportes.
- Aprovechar `TokenInfo` para resaltar tokens en la entrada o crear un árbol
  sintáctico visual.

Con esta documentación debería ser sencillo comprender el propósito de cada
archivo, su interacción dentro del proyecto y los motivos detrás de las
herramientas utilizadas.
