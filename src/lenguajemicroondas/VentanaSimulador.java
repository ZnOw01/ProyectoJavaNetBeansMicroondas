package lenguajemicroondas;

import Cup.LexerCup;
import Cup.Syntactic;
import Cup.sym;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java_cup.runtime.Symbol;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class VentanaSimulador extends JFrame {

    private JTextField display;
    private JPanel panelBotones;
    private JTextArea logArea;
    private Timer timerSimulacion;
    
    private JLabel panelPuertaAnimado;
    private ImageIcon iconoCocinando;
    private ImageIcon iconoDetenido;

    private final StringBuilder comandoParaParser = new StringBuilder();
    private final StringBuilder bufferNumerico = new StringBuilder();
    private enum ModoEntrada { NADA, TIEMPO, POTENCIA }
    private ModoEntrada modoActual = ModoEntrada.NADA;
    private int tiempoSegundos = 0;

    private static final int MAX_DIGITOS = 5;
    private static final Font FONT_DISPLAY = new Font("Monospaced", Font.BOLD, 32);
    private static final Font FONT_BOTON_NUMERO = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_LOG = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    
    private static final Color COLOR_DISPLAY_FONDO = Color.BLACK;
    private static final Color COLOR_DISPLAY_TEXTO = new Color(0, 255, 100);
    private static final Color COLOR_PUERTA_FONDO = new Color(50, 50, 50);
    private static final Color COLOR_BOTON_INICIO = new Color(40, 167, 69);
    private static final Color COLOR_BOTON_LIMPIAR = new Color(255, 193, 7);

    public VentanaSimulador() {
        super("Simulador de Microondas");
        initLookAndFeel();
        cargarRecursos();
        initComponents();
        limpiarTodo();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Logger.getLogger(VentanaSimulador.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    private void cargarRecursos() {
        try {
            iconoCocinando = new ImageIcon(getClass().getResource("/lenguajemicroondas/resources/cooking.gif"));
            iconoDetenido = new ImageIcon(getClass().getResource("/lenguajemicroondas/resources/stopped.png"));
        } catch (Exception e) {
            log("ADVERTENCIA: No se pudieron cargar los íconos de recursos. La animación no funcionará.");
            log(e.getMessage());
            iconoCocinando = null;
            iconoDetenido = null;
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane panelPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        panelPrincipal.setResizeWeight(0.7);
        panelPrincipal.setBorder(null);

        panelPrincipal.setLeftComponent(crearPanelSimulacion());
        panelPrincipal.setRightComponent(crearPanelControl());

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private JPanel crearPanelControl() {
        JPanel panelControl = new JPanel(new BorderLayout(10, 10));
        panelControl.setPreferredSize(new Dimension(240, 450));

        display = new JTextField("00:00");
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(FONT_DISPLAY);
        display.setBackground(COLOR_DISPLAY_FONDO);
        display.setForeground(COLOR_DISPLAY_TEXTO);
        display.setBorder(BorderFactory.createLoweredBevelBorder());
        panelControl.add(display, BorderLayout.NORTH);

        panelBotones = new JPanel(new GridLayout(6, 3, 5, 5));
        panelBotones.add(crearBotonComando("ON", "encender"));
        panelBotones.add(crearBotonComando("ABRIR", "abrir"));
        panelBotones.add(crearBotonComando("CERRAR", "cerrar"));

        panelBotones.add(crearBotonModo("POT", ModoEntrada.POTENCIA));
        panelBotones.add(crearBotonModo("TIEMPO", ModoEntrada.TIEMPO));
        panelBotones.add(crearBotonComando("PAUSA", "pausar"));

        panelBotones.add(crearBotonNumero("7"));
        panelBotones.add(crearBotonNumero("8"));
        panelBotones.add(crearBotonNumero("9"));

        panelBotones.add(crearBotonNumero("4"));
        panelBotones.add(crearBotonNumero("5"));
        panelBotones.add(crearBotonNumero("6"));

        panelBotones.add(crearBotonNumero("1"));
        panelBotones.add(crearBotonNumero("2"));
        panelBotones.add(crearBotonNumero("3"));

        panelBotones.add(crearBotonInicio("INICIO"));
        panelBotones.add(crearBotonNumero("0"));
        panelBotones.add(crearBotonLimpiar("CLR"));

        panelControl.add(panelBotones, BorderLayout.CENTER);
        return panelControl;
    }

    private JSplitPane crearPanelSimulacion() {
        panelPuertaAnimado = new JLabel();
        panelPuertaAnimado.setBackground(COLOR_PUERTA_FONDO);
        panelPuertaAnimado.setOpaque(true);
        panelPuertaAnimado.setHorizontalAlignment(SwingConstants.CENTER);
        panelPuertaAnimado.setBorder(BorderFactory.createLoweredBevelBorder());
        panelPuertaAnimado.setMinimumSize(new Dimension(200, 200));
        panelPuertaAnimado.setIcon(iconoDetenido);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(FONT_LOG);
        logArea.setMargin(new Insets(5, 5, 5, 5));
        
        JScrollPane scrollLogs = new JScrollPane(logArea);
        scrollLogs.setBorder(new TitledBorder("Consola de Análisis"));
        scrollLogs.setMinimumSize(new Dimension(200, 100));

        JSplitPane panelSimulacion = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        panelSimulacion.setTopComponent(panelPuertaAnimado);
        panelSimulacion.setBottomComponent(scrollLogs);
        panelSimulacion.setResizeWeight(0.8);
        panelSimulacion.setBorder(null);
        
        return panelSimulacion;
    }

    private JButton crearBotonNumero(String numero) {
        JButton btn = new JButton(numero);
        btn.setFont(FONT_BOTON_NUMERO);
        btn.addActionListener(e -> onNumeroPulsado(numero));
        return btn;
    }

    private JButton crearBotonComando(String etiqueta, String comando) {
        JButton btn = new JButton(etiqueta);
        btn.addActionListener(e -> onComandoPulsado(etiqueta, comando));
        return btn;
    }

    private JButton crearBotonModo(String etiqueta, ModoEntrada modo) {
        JButton btn = new JButton(etiqueta);
        btn.addActionListener(e -> onModoPulsado(etiqueta, modo));
        return btn;
    }

    private JButton crearBotonInicio(String etiqueta) {
        JButton btn = new JButton(etiqueta);
        btn.setBackground(COLOR_BOTON_INICIO);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.addActionListener(e -> onInicioPulsado());
        return btn;
    }

    private JButton crearBotonLimpiar(String etiqueta) {
        JButton btn = new JButton(etiqueta);
        btn.setBackground(COLOR_BOTON_LIMPIAR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.addActionListener(e -> onLimpiarPulsado());
        return btn;
    }

    private void onNumeroPulsado(String numero) {
        if (modoActual == ModoEntrada.NADA) {
            display.setText("E-MODO");
            log("ERROR: Intento de ingresar número sin modo (Tiempo/Potencia).");
            return;
        }

        if (bufferNumerico.length() >= MAX_DIGITOS) {
            display.setText("E-MAX");
            log("ERROR: Se ha superado el límite de dígitos (" + MAX_DIGITOS + ").");
            return;
        }

        bufferNumerico.append(numero);
        display.setText(bufferNumerico.toString());
    }

    private void onComandoPulsado(String etiqueta, String comando) {
        finalizarEntradaNumerica();
        comandoParaParser.append(comando).append(" ");
        display.setText(etiqueta);
        log("Comando añadido: " + comando);
    }
    
    private void onModoPulsado(String etiqueta, ModoEntrada modo) {
        finalizarEntradaNumerica();
        modoActual = modo;
        String comando = modo.toString().toLowerCase();
        comandoParaParser.append(comando).append(" ");
        display.setText("0");
        log("Modo activado: " + comando + ". Esperando número.");
    }
    
    private void onInicioPulsado() {
        finalizarEntradaNumerica();
        comandoParaParser.append("cocinar ");
        log("Comando añadido: cocinar");
        ejecutarAnalisis();
    }
    
    private void onLimpiarPulsado() {
        limpiarTodo();
    }

    private void finalizarEntradaNumerica() {
        if (modoActual == ModoEntrada.NADA || bufferNumerico.length() == 0) {
            return;
        }

        String valor = bufferNumerico.toString();
        comandoParaParser.append(valor).append(" ");
        log("Valor numérico añadido para " + modoActual + ": " + valor);

        try {
            int valorNumerico = Integer.parseInt(valor);
            if (modoActual == ModoEntrada.TIEMPO) {
                tiempoSegundos = valorNumerico;
            }
        } catch (NumberFormatException e) {
            log("ERROR: El número '" + valor + "' es demasiado grande.");
            display.setText("E-NUM");
        }

        bufferNumerico.setLength(0);
        modoActual = ModoEntrada.NADA;
    }

    private void ejecutarAnalisis() {
        String textoCompleto = comandoParaParser.toString().trim();
        log("\n--- INICIANDO ANÁLISIS ---");
        log("Entrada: " + textoCompleto);

        Reader reader = new StringReader(textoCompleto);
        LexerCup lexerCup = new LexerCup(reader);
        Syntactic parser = new Syntactic(lexerCup);

        try {
            parser.parse();
            log("ANÁLISIS SINTÁCTICO EXITOSO.");
            
            if (tiempoSegundos == 0) {
                 display.setText("E-TIEMPO");
                 log("ERROR LÓGICO: El comando es sintácticamente correcto, pero no se especificó tiempo.");
                 limpiarTodo();
                 return;
            }
            
            log("Iniciando simulación por " + tiempoSegundos + " segundos.");
            iniciarSimulacion(tiempoSegundos);

        } catch (Exception ex) {
            display.setText("E-SINTAXIS");
            log("--- ERROR DE SINTAXIS DETECTADO ---");
            
            Symbol symbolError = parser.getS();
            if (symbolError != null) {
                int line = symbolError.left + 1;
                int column = symbolError.right + 1;
                String tokenValue = symbolError.value != null ? symbolError.value.toString() : "desconocido";
                String tokenName = tokenName(symbolError.sym);

                log("Token problemático: '" + tokenValue + "' (Tipo: " + tokenName + ")");
                log("Ubicación: Línea " + line + ", Columna " + column);
                log("Verifica el orden o la validez de los comandos.");
            } else {
                log("No se pudo determinar la ubicación exacta del error.");
            }
            log("--- ANÁLISIS FALLIDO ---");
            limpiarTodo();
        }
    }

    private void iniciarSimulacion(int segundos) {
        if (panelPuertaAnimado != null) {
            panelPuertaAnimado.setIcon(iconoCocinando);
        }
        
        final int[] segundosRestantes = {segundos};

        int minutos = segundosRestantes[0] / 60;
        int segs = segundosRestantes[0] % 60;
        display.setText(String.format("%02d:%02d", minutos, segs));
        
        timerSimulacion = new Timer(1000, e -> {
            segundosRestantes[0]--;

            if (segundosRestantes[0] < 0) {
                timerSimulacion.stop();
                display.setText("FIN");
                log("Simulación finalizada.");
                limpiarTodo();
            } else {
                int min = segundosRestantes[0] / 60;
                int seg = segundosRestantes[0] % 60;
                display.setText(String.format("%02d:%02d", min, seg));
            }
        });

        timerSimulacion.start();
    }

    private void limpiarTodo() {
        comandoParaParser.setLength(0);
        bufferNumerico.setLength(0);
        modoActual = ModoEntrada.NADA;
        tiempoSegundos = 0;
        
        if (timerSimulacion != null) {
            timerSimulacion.stop();
        }
        
        if (panelPuertaAnimado != null) {
            panelPuertaAnimado.setIcon(iconoDetenido);
        }
        
        display.setText("00:00");
        
        if (logArea != null) {
            logArea.setText("");
            log("Simulador listo. Ingrese comandos.");
        }
    }

    private void log(String mensaje) {
        if (logArea != null) {
            logArea.append(mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } else {
            System.out.println(mensaje);
        }
    }

    private static String tokenName(int id) {
        return switch (id) {
            case sym.Numero -> "Numero";
            case sym.DOS_PUNTOS -> "DOS_PUNTOS";
            case sym.MAS -> "MAS";
            case sym.MENOS -> "MENOS";
            case sym.ERROR -> "ERROR";
            case sym.Abrir -> "Abrir";
            case sym.Cerrar -> "Cerrar";
            case sym.Encender -> "Encender";
            case sym.Apagar -> "Apagar";
            case sym.Potencia -> "Potencia";
            case sym.Pausar -> "Pausar";
            case sym.Cocinar -> "Cocinar";
            case sym.Tiempo -> "Tiempo";
            case sym.EOF -> "EOF";
            case sym.error -> "error (sintáctico)";
            default -> "TOK(" + id + ")";
        };
    }
}