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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import java_cup.runtime.Symbol;

public class Ventana extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(Ventana.class.getName());
    private static final String APP_TITLE = "Lenguaje Microondas";

    private final JTextArea inputArea = createTextArea(true);
    private final JTextArea lexicalResultArea = createTextArea(false);
    private final JTextArea syntacticResultArea = createTextArea(false);
    private final JFileChooser fileChooser = new JFileChooser();

    private String lastLexicalReport = "";
    private String lastSyntacticReport = "";

    public Ventana() {
        super(APP_TITLE);
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        add(createToolbar(), BorderLayout.NORTH);
        add(createResultsPanel(), BorderLayout.CENTER);
    }

    /* Barra de herramientas */
    private JPanel createToolbar() {
        JButton openButton = new JButton("Abrir archivo");
        openButton.addActionListener(e -> loadFile());

        JButton saveInputButton = new JButton("Guardar entrada");
        saveInputButton.addActionListener(e -> saveInput());

        JButton analyzeButton = new JButton("Analizar");
        analyzeButton.addActionListener(e -> analyzeInput());

        JButton saveReportButton = new JButton("Guardar reporte");
        saveReportButton.addActionListener(e -> saveReports());

        JButton clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> clearAll());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.add(openButton);
        buttonPanel.add(saveInputButton);
        buttonPanel.add(analyzeButton);
        buttonPanel.add(saveReportButton);
        buttonPanel.add(clearButton);

        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.add(buttonPanel, BorderLayout.WEST);

        return toolbar;
    }

    /* Panel de resultados con tres columnas */
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel columnContainer = new JPanel();
        columnContainer.setLayout(new java.awt.GridLayout(1, 3, 10, 0));

        columnContainer.add(createScrollPane("Entrada", inputArea));
        columnContainer.add(createScrollPane("Análisis léxico", lexicalResultArea));
        columnContainer.add(createScrollPane("Análisis sintáctico", syntacticResultArea));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(columnContainer, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createScrollPane(String title, JTextArea textArea) {
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

    /* Cargar archivo desde el disco */
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
                LOGGER.log(Level.SEVERE, "Error al leer archivo", ex);
                showError("No se pudo leer el archivo.");
            }
        }
    }

    /* Guardar contenido de la entrada */
    private void saveInput() {
        String text = inputArea.getText().trim();

        if (text.isEmpty()) {
            showWarning("No hay contenido para guardar.");
            return;
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Path path = selectedFile.toPath();

            try {
                Files.writeString(path, text, StandardCharsets.UTF_8);
                showInformation("Archivo guardado exitosamente.");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error al guardar archivo", ex);
                showError("No se pudo guardar el archivo.");
            }
        }
    }

    /* Ejecutar análisis léxico y sintáctico */
    private void analyzeInput() {
        String text = inputArea.getText().trim();

        if (text.isEmpty()) {
            showWarning("No hay texto para analizar.");
            return;
        }

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
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
            LOGGER.log(Level.SEVERE, "Error durante análisis", ex);
            showError("Error al ejecutar el análisis.");
        }
    }

    /* Análisis léxico: tokeniza el texto */
    private String runLexicalAnalysis(String text) throws IOException {
        Reader reader = new StringReader(text);
        LexerCup lexer = new LexerCup(reader);
        StringBuilder report = new StringBuilder();

        Symbol token;
        while ((token = lexer.next_token()).sym != sym.EOF) {
            String tokenType = getTokenName(token.sym);
            String tokenValue = token.value != null ? token.value.toString() : "";
            int line = token.left + 1;
            int column = token.right + 1;

            report.append(String.format("%-15s %-10s L:%d C:%d%n",
                    tokenType, tokenValue, line, column));
        }

        return report.toString();
    }

    /* Análisis sintáctico: verifica la gramática */
    private String runSyntacticAnalysis(String text) {
        Reader reader = new StringReader(text);
        LexerCup lexer = new LexerCup(reader);
        Syntactic parser = new Syntactic(lexer);
        StringBuilder report = new StringBuilder();

        try {
            parser.parse();
            report.append("✓ Análisis sintáctico exitoso\n");
            report.append("El programa cumple con la gramática.\n");
        } catch (Exception ex) {
            report.append("✗ Error sintáctico\n\n");

            Symbol errorSymbol = parser.getS();
            if (errorSymbol != null) {
                int line = errorSymbol.left + 1;
                int column = errorSymbol.right + 1;
                String tokenValue = errorSymbol.value != null ? errorSymbol.value.toString() : "?";

                report.append("Token problemático: ").append(tokenValue).append("\n");
                report.append("Ubicación: Línea ").append(line).append(", Columna ").append(column).append("\n\n");
            }

            report.append("Detalles: ").append(ex.getMessage());
        }

        return report.toString();
    }

    /* Guardar reporte de análisis */
    private void saveReports() {
        if (lastLexicalReport.isEmpty() && lastSyntacticReport.isEmpty()) {
            showWarning("Realiza un análisis antes de guardar el reporte.");
            return;
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Path path = selectedFile.toPath();

            StringBuilder fullReport = new StringBuilder();
            fullReport.append("=== ANÁLISIS LÉXICO ===\n\n");
            fullReport.append(lastLexicalReport);
            fullReport.append("\n\n=== ANÁLISIS SINTÁCTICO ===\n\n");
            fullReport.append(lastSyntacticReport);

            try {
                Files.writeString(path, fullReport.toString(), StandardCharsets.UTF_8);
                showInformation("Reporte guardado en: " + path);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error al guardar reporte", ex);
                showError("No se pudo guardar el reporte.");
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

    /* Mapeo de IDs de tokens a nombres legibles */
    private String getTokenName(int tokenId) {
        return switch (tokenId) {
            case sym.Inicio -> "INICIO";
            case sym.Final -> "FINAL";
            case sym.Abrir -> "ABRIR";
            case sym.Cerrar -> "CERRAR";
            case sym.Encender -> "ENCENDER";
            case sym.Apagar -> "APAGAR";
            case sym.Potencia -> "POTENCIA";
            case sym.Pausar -> "PAUSAR";
            case sym.Reanudar -> "REANUDAR";
            case sym.Cocinar -> "COCINAR";
            case sym.Tiempo -> "TIEMPO";
            case sym.Numero -> "NUMERO";
            case sym.DOS_PUNTOS -> "DOS_PUNTOS";
            case sym.MAS -> "MAS";
            case sym.MENOS -> "MENOS";
            case sym.ERROR -> "ERROR";
            default -> "DESCONOCIDO";
        };
    }
}
