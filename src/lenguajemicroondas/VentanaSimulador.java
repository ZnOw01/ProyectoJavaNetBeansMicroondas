package lenguajemicroondas;

import Cup.LexerCup;
import Cup.Syntactic;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;

public class VentanaSimulador extends JFrame {

    // --- Componentes Visuales ---
    private JTextField display;
    private JPanel panelBotones;
    private JPanel panelPuerta;
    private Timer timerSimulacion;

    // --- Variables para Enfoque 1 ---
    private final StringBuilder comandoParaParser = new StringBuilder();
    private final StringBuilder bufferNumerico = new StringBuilder();
    private enum ModoEntrada { NADA, TIEMPO, POTENCIA }
    private ModoEntrada modoActual = ModoEntrada.NADA;
    private int tiempoSegundos = 0;
    
    // --- CAMBIO: Constante para limitar la entrada ---
    private static final int MAX_DIGITOS = 5; 

    public VentanaSimulador() {
        super("Simulador de Microondas");

        // 1. Aplicar un diseño más moderno
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { /* Usar por defecto */ }
        }

        // 2. Configurar la ventana
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10));
        // --- CAMBIO: Ajuste de tamaño para que quepan los botones ---
        setMinimumSize(new Dimension(750, 400)); 
        setLocationRelativeTo(null);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 3. Crear y añadir los componentes
        initComponents();
    }

    private void initComponents() {
        // ---- 1. La "Puerta" (Panel Izquierdo) ----
        panelPuerta = new JPanel();
        panelPuerta.setBackground(new Color(50, 50, 50));
        panelPuerta.setBorder(BorderFactory.createLoweredBevelBorder());
        panelPuerta.setPreferredSize(new Dimension(450, 350)); // <-- CAMBIO: Ancho ajustado
        add(panelPuerta, BorderLayout.CENTER);

        // ---- 2. El Panel de Control (Panel Derecho) ----
        JPanel panelControl = new JPanel(new BorderLayout(10, 10));
        panelControl.setPreferredSize(new Dimension(220, 350)); // <-- CAMBIO: Ancho ajustado
        
        // ---- 2a. El Display (Arriba) ----
        display = new JTextField("00:00");
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font("Monospaced", Font.BOLD, 32));
        display.setBackground(Color.BLACK);
        display.setForeground(new Color(0, 255, 100)); // Verde digital
        display.setBorder(BorderFactory.createLoweredBevelBorder());
        panelControl.add(display, BorderLayout.NORTH);

        // ---- 2b. La Botonera (Centro) ----
        panelBotones = new JPanel(new GridLayout(6, 3, 5, 5)); // 6 filas, 3 columnas
        
        // --- CAMBIO: Textos de botones abreviados (como en tu captura) ---
        // Fila 1
        panelBotones.add(crearBotonComando("ON", "encender")); 
        panelBotones.add(crearBotonComando("ABRIR", "abrir"));
        panelBotones.add(crearBotonComando("CERRAR", "cerrar"));
        
        // Fila 2
        panelBotones.add(crearBotonModo("POT", ModoEntrada.POTENCIA));
        panelBotones.add(crearBotonModo("TIEMPO", ModoEntrada.TIEMPO));
        panelBotones.add(crearBotonComando("PAUSA", "pausar"));

        // Fila 3
        panelBotones.add(crearBotonNumero("7"));
        panelBotones.add(crearBotonNumero("8"));
        panelBotones.add(crearBotonNumero("9"));

        // Fila 4
        panelBotones.add(crearBotonNumero("4"));
        panelBotones.add(crearBotonNumero("5"));
        panelBotones.add(crearBotonNumero("6"));

        // Fila 5
        panelBotones.add(crearBotonNumero("1"));
        panelBotones.add(crearBotonNumero("2"));
        panelBotones.add(crearBotonNumero("3"));

        // Fila 6
        panelBotones.add(crearBotonInicio("INICIO")); 
        panelBotones.add(crearBotonNumero("0"));
        panelBotones.add(crearBotonLimpiar("CLR")); // "LIMPIAR" -> "CLR"

        panelControl.add(panelBotones, BorderLayout.CENTER);
        add(panelControl, BorderLayout.EAST);
    }

    // --- Métodos para crear botones ---

    /** Botón para números (1, 2, 3...) */
    private JButton crearBotonNumero(String numero) {
        JButton btn = new JButton(numero);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.addActionListener(e -> {
            if (modoActual == ModoEntrada.NADA) {
                display.setText("E-MODO");
                return;
            }

            // --- CAMBIO: Limitar la longitud del número ---
            if (bufferNumerico.length() >= MAX_DIGITOS) { 
                display.setText("E-MAX"); // Error de Max dígitos
                return; // No añadir más números
            }
            // ------------------------------------------

            bufferNumerico.append(numero);
            display.setText(bufferNumerico.toString());
        });
        return btn;
    }

    /** Botón de "Comando Directo" (Abrir, Encender, Pausar...) */
    private JButton crearBotonComando(String etiqueta, String comando) {
        JButton btn = new JButton(etiqueta);
        btn.addActionListener(e -> {
            finalizarEntradaNumerica(); 
            comandoParaParser.append(comando).append(" ");
            display.setText(etiqueta);
            System.out.println("Comandos: " + comandoParaParser.toString());
        });
        return btn;
    }

    /** Botón que "activa un modo" (Tiempo, Potencia) */
    private JButton crearBotonModo(String etiqueta, ModoEntrada modo) {
        JButton btn = new JButton(etiqueta);
        btn.addActionListener(e -> {
            finalizarEntradaNumerica();
            modoActual = modo;
            // --- CAMBIO: Usar el comando real, no la etiqueta ---
            comandoParaParser.append(modo.toString().toLowerCase()).append(" "); // "tiempo " o "potencia "
            display.setText("0");
        });
        return btn;
    }

    /** El botón de INICIO (Cocinar) */
    private JButton crearBotonInicio(String etiqueta) {
        JButton btn = new JButton(etiqueta);
        btn.setBackground(Color.GREEN); 
        btn.addActionListener(e -> {
            finalizarEntradaNumerica();
            comandoParaParser.append("cocinar ");
            ejecutarAnalisis();
        });
        return btn;
    }

    /** Botón para Limpiar todo */
    private JButton crearBotonLimpiar(String etiqueta) {
        JButton btn = new JButton(etiqueta);
        btn.setBackground(Color.ORANGE);
        btn.addActionListener(e -> {
            limpiarTodo();
        });
        return btn;
    }

    /** Limpia todas las variables de estado */
    private void limpiarTodo() {
        comandoParaParser.setLength(0);
        bufferNumerico.setLength(0);
        modoActual = ModoEntrada.NADA;
        tiempoSegundos = 0;
        if (timerSimulacion != null) {
            timerSimulacion.stop();
        }
        display.setText("00:00");
    }

    /**
     * Esta función "cierra" una entrada numérica.
     * Añade protección contra números demasiado grandes.
     */
    private void finalizarEntradaNumerica() {
        if (modoActual == ModoEntrada.NADA || bufferNumerico.length() == 0) {
            return; // No hay nada que finalizar
        }

        String valor = bufferNumerico.toString();
        comandoParaParser.append(valor).append(" "); 

        // --- CAMBIO: try-catch para evitar el crash (NumberFormatException) ---
        try { 
            int valorNumerico = Integer.parseInt(valor);
            
            if (modoActual == ModoEntrada.TIEMPO) {
                tiempoSegundos = valorNumerico;
            }
            // (Aquí podrías guardar 'potenciaNivel' si quisieras)
            // if (modoActual == ModoEntrada.POTENCIA) { ... }

        } catch (NumberFormatException e) {
            // El número es demasiado grande (ej. "854445559555")
            System.out.println("Error: Número demasiado grande: " + valor);
            display.setText("E-NUM"); // Error de Número
            // No limpiamos el parser, dejamos que el análisis sintáctico falle si es necesario
        }
        // -----------------------------------------------------------------

        // Reseteamos para la próxima entrada
        bufferNumerico.setLength(0);
        modoActual = ModoEntrada.NADA;
    }
    
    /**
     * ¡EL NÚCLEO DEL ENFOQUE 1!
     * Toma el string completo y se lo da al parser.
     */
    private void ejecutarAnalisis() {
        String textoCompleto = comandoParaParser.toString();
        
        System.out.println("--- ANALIZANDO COMANDOS ---");
        System.out.println(textoCompleto);
        
        Reader reader = new StringReader(textoCompleto);
        LexerCup lexerCup = new LexerCup(reader);
        Syntactic parser = new Syntactic(lexerCup);

        try {
            parser.parse();
            System.out.println("¡Análisis Sintáctico Exitoso!");
            
            // Si el tiempo es 0 después de un parseo exitoso,
            // es un error lógico (ej. "potencia 5 cocinar")
            if (tiempoSegundos == 0) {
                 display.setText("E-TIEMPO");
                 limpiarTodo();
                 return;
            }
            
            iniciarSimulacion(tiempoSegundos); 

        } catch (Exception ex) {
            System.out.println("Error de sintaxis: " + ex.getMessage());
            display.setText("E-SINTAXIS"); 
            limpiarTodo();
        }
    }

    /**
     * Inicia el Timer de cuenta regresiva.
     */
    private void iniciarSimulacion(int segundos) {
        // Esta comprobación ahora se hace en ejecutarAnalisis
        // if (segundos <= 0) ...

        final int[] segundosRestantes = {segundos};
        
        // Formatea MM:SS
        int minutos = segundosRestantes[0] / 60;
        int segs = segundosRestantes[0] % 60;
        display.setText(String.format("%02d:%02d", minutos, segs));
        
        timerSimulacion = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                segundosRestantes[0]--; 

                if (segundosRestantes[0] < 0) {
                    timerSimulacion.stop();
                    display.setText("FIN");
                    limpiarTodo(); 
                } else {
                    int min = segundosRestantes[0] / 60;
                    int seg = segundosRestantes[0] % 60;
                    display.setText(String.format("%02d:%02d", min, seg));
                }
            }
        });
        
        timerSimulacion.start(); 
    }   
}