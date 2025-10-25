package lenguajemicroondas;

import Cup.LexerCup;
import Cup.Syntactic;
import Cup.sym;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import java_cup.runtime.Symbol;

/**
 * Ventana principal de la aplicación del Lenguaje Microondas.
 * Muestra análisis léxico y sintáctico usando JFlex + CUP (LexerCup / Syntactic).
 */
public class Ventana extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(Ventana.class.getName());
    private static final String APPLICATION_TITLE = "Lenguaje Microondas";

    private final JTextArea inputArea = createTextArea(true);
    private final JTextArea lexicalResultArea = createTextArea(false);
    private final JTextArea syntacticResultArea = createTextArea(false);

    private final JFileChooser fileChooser = new JFileChooser();

    private String lastLexicalReport = "";
    private String lastSyntacticReport = "";

    public Ventana() {
        super(APPLICATION_TITLE);
        configureWindow();
        add(createToolbar(), BorderLayout.NORTH);
        add(createResultsPanel(), BorderLayout.CENTER);
    }

    /* ----------------------------- UI ------------------------------ */

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
    }

    private JPanel createToolbar() {
        JButton openButton = new JButton("Abrir archivo");
        openButton.addActionListener(event -> loadFile());

        JButton analyzeButton = new JButton("Analizar");
        analyzeButton.addActionListener(event -> analyzeInput());

        JButton saveButton = new JButton("Guardar reporte");
        saveButton.addActionListener(event -> saveReports());

        JButton clearButton = new JButton("Limpiar");
        clearButton.addActionListener(event -> clearAll());

        JLabel instructions = new JLabel("Carga un archivo o escribe comandos y presiona \"Analizar\".");
        instructions.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.add(openButton);
        buttonPanel.add(analyzeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);

        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.add(buttonPanel, BorderLayout.WEST);
        toolbar.add(instructions, BorderLayout.CENTER);

        return toolbar;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel columnContainer = new JPanel();
        columnContainer.setLayout(new java.awt.GridLayout(1, 3, 10, 0));
        columnContainer.add(createTitledScrollPane("Entrada", inputArea));
        columnContainer.add(createTitledScrollPane("Análisis léxico", lexicalResultArea));
        columnContainer.add(createTitledScrollPane("Análisis sintáctico", syntacticResultArea));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(columnContainer, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTitledScrollPane(String title, JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new TitledBorder(title));
        scrollPane.setPreferredSize(new Dimension(320, 420));
        return scrollPane;
    }

    private static JTextArea createTextArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setMargin(new Insets(8, 8, 8, 8));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        if (!editable) {
            area.setBackground(new java.awt.Color(245, 245, 245));
        }
        return area;
    }

    /* --------------------------- Acciones -------------------------- */

    private void loadFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Path path = selectedFile.toPath();
            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                inputArea.setText(content);
                inputArea.setCaretPosition(0);
                lastLexicalReport = "";
                lastSyntacticReport = "";
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "No se pudo leer el archivo", ex);
                showError("No fue posible leer el archivo seleccionado.");
            }
        }
    }

    private void analyzeInput() {
        String text = inputArea.getText().trim();

        if (text.isEmpty()) {
            showWarning("No hay texto para analizar.");
            return;
        }

        // Al escribir, limpia resultados anteriores
        inputArea.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                lastLexicalReport = "";
                lastSyntacticReport = "";
            }
        });

        try {
            lastLexicalReport = runLexicalAnalysis(text);
            lexicalResultArea.setText(lastLexicalReport);
            lexicalResultArea.setCaretPosition(0);

            lastSyntacticReport = runSyntacticAnalysis(text);
            syntacticResultArea.setText(lastSyntacticReport);
            syntacticResultArea.setCaretPosition(0);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error durante el análisis léxico", ex);
            showError("Ocurrió un error al ejecutar el análisis léxico.");
        }
    }

    /* ---------------------- Análisis léxico ------------------------ */

    /** Realiza el escaneo con Cup.LexerCup y devuelve un reporte amigable. */
    private String runLexicalAnalysis(String text) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("=== ANÁLISIS LÉXICO DEL MICROONDAS ===\n\n");

        Reader reader = new StringReader(text);
        LexerCup lexer = new LexerCup(reader);

        int tokenCount = 0;
        int errorCount = 0;

        while (true) {
            Symbol tok = lexer.next_token();
            if (tok == null || tok.sym == sym.EOF) break;

            tokenCount++;
            String name = tokenName(tok.sym);

            if (tok.sym == sym.ERROR) {
                report.append("ERROR: Carácter no válido '")
                      .append(tok.value)
                      .append("'\n");
                errorCount++;
                continue;
            }

            // Mostrar tokens relevantes con su lexema/valor
            switch (tok.sym) {
                case sym.Numero -> report.append("[ Numero: ").append(tok.value).append(" ]\n");
                case sym.Abrir, sym.Cerrar, sym.Encender, sym.Apagar,
                     sym.Potencia, sym.Pausar, sym.Cocinar, sym.Tiempo ->
                        report.append("[ Comando: ").append(tok.value)
                              .append(" : ").append(name).append(" ]\n");
                case sym.DOS_PUNTOS, sym.MAS, sym.MENOS ->
                        report.append("[ Símbolo: ").append(name).append(" ]\n");
                default ->
                        report.append("[ Token: ").append(name).append(" ]\n");
            }
        }

        report.append("\n=== RESUMEN ===\n");
        report.append("Total de tokens encontrados: ").append(tokenCount).append('\n');
        report.append("Errores léxicos: ").append(errorCount).append('\n');
        report.append("=== FIN DEL ANÁLISIS ===");

        return report.toString();
    }

    /* -------------------- Análisis sintáctico ---------------------- */

    private String runSyntacticAnalysis(String text) {
        StringBuilder report = new StringBuilder();
        report.append("=== ANÁLISIS SINTÁCTICO DEL MICROONDAS ===\n\n");

        Reader reader = new StringReader(text);
        LexerCup lexerCup = new LexerCup(reader);
        Syntactic parser = new Syntactic(lexerCup);

        try {
            parser.parse();
            report.append("ANÁLISIS COMPLETADO EXITOSAMENTE\n\n");
            report.append("El código cumple con la sintaxis correcta del lenguaje del microondas.\n\n");
            report.append("Estructura válida detectada:\n");
            report.append("- Comandos reconocidos correctamente\n");
            report.append("- Secuencia de instrucciones válida\n");
            report.append("- Sin errores sintácticos\n\n");
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Fallo en el análisis sintáctico", ex);
            report.append("ERROR SINTÁCTICO DETECTADO\n\n");

            Symbol symbolError = parser.getS();
            if (symbolError != null) {
                int line = symbolError.left + 1;
                int column = symbolError.right + 1;
                String tokenValue = symbolError.value != null ? symbolError.value.toString() : "desconocido";

                report.append("Ubicación del error:\n");
                report.append("  - Línea: ").append(line).append('\n');
                report.append("  - Columna: ").append(column).append('\n');
                report.append("  - Token problemático: ").append(tokenValue).append("\n\n");

                report.append("Posibles soluciones:\n");
                report.append("  • Verifica que los comandos estén bien escritos\n");
                report.append("  • Revisa que la sintaxis de Potencia incluya número (opcional + o -)\n");
                report.append("  • Confirma que Tiempo tenga formato número o número:número\n");
                report.append("  • Asegúrate de que los comandos estén en el orden correcto\n\n");
            } else {
                report.append("No se pudo determinar la ubicación exacta del error.\n");
                report.append("Revisa la estructura general del código.\n\n");
            }
        }

        report.append("=== FIN DEL ANÁLISIS SINTÁCTICO ===");
        return report.toString();
    }

    /* ----------------------------- Utils --------------------------- */

    private static String tokenName(int id) {
        // Mapea ids de sym.* a nombres legibles
        if (id == sym.Numero)      return "Numero";
        if (id == sym.DOS_PUNTOS)  return "DOS_PUNTOS";
        if (id == sym.MAS)         return "MAS";
        if (id == sym.MENOS)       return "MENOS";
        if (id == sym.ERROR)       return "ERROR";
        if (id == sym.Abrir)       return "Abrir";
        if (id == sym.Cerrar)      return "Cerrar";
        if (id == sym.Encender)    return "Encender";
        if (id == sym.Apagar)      return "Apagar";
        if (id == sym.Potencia)    return "Potencia";
        if (id == sym.Pausar)      return "Pausar";
        if (id == sym.Cocinar)     return "Cocinar";
        if (id == sym.Tiempo)      return "Tiempo";
        if (id == sym.EOF)         return "EOF";
        return "TOK(" + id + ")";
    }

    private void saveReports() {
        if (lastLexicalReport.isBlank() && lastSyntacticReport.isBlank()) {
            showWarning("Realiza un análisis antes de guardar el reporte.");
            return;
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destination = fileChooser.getSelectedFile();
            Path path = destination.toPath();
            if (!path.toString().toLowerCase().endsWith(".txt")) {
                path = path.resolveSibling(path.getFileName() + ".txt");
            }

            StringBuilder builder = new StringBuilder();
            if (!lastLexicalReport.isBlank()) {
                builder.append(lastLexicalReport).append("\n\n");
            }
            if (!lastSyntacticReport.isBlank()) {
                builder.append(lastSyntacticReport);
            }

            try {
                Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
                showInformation("Reporte guardado en: " + path);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "No se pudo guardar el archivo", ex);
                showError("No fue posible guardar el reporte.");
            }
        }
    }

    private void clearAll() {
        inputArea.setText("");
        lexicalResultArea.setText("");
        syntacticResultArea.setText("");
        lastLexicalReport = "";
        lastSyntacticReport = "";
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInformation(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
