package lenguajemicroondas;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LenguajeMicroondas {

    private static final Path SOURCE_DIRECTORY = Paths.get("src", "lenguajemicroondas");
    private static final Path CUP_DIRECTORY = Paths.get("src", "Cup");
    private static final Path PROJECT_ROOT = Paths.get("");

    private LenguajeMicroondas() {
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Ventana().setVisible(true));
    }

    public static void checkParser() {
        EventQueue.invokeLater(() -> new Ventana().setVisible(true));
    }

    public static void checkLexer() {
        EventQueue.invokeLater(() -> new Ventana().setVisible(true));
    }

    /* Regenera el analizador léxico desde entrada.jflex */
    public static void generarLexer() {
        Path lexerPath = SOURCE_DIRECTORY.resolve("entrada.jflex");
        File archivo = lexerPath.toFile();

        if (!archivo.exists()) {
            throw new IllegalStateException("No se encontró: " + lexerPath);
        }

        JFlex.Main.generate(archivo);
    }

    /* Regenera el analizador sintáctico desde Grammar.cup */
    public static void generarCup() throws Exception {
        Path cupFile = CUP_DIRECTORY.resolve("Grammar.cup");

        if (!Files.exists(cupFile)) {
            throw new IllegalStateException("No se encontró: " + cupFile);
        }

        String[] cupArguments = {"-parser", "Syntactic", cupFile.toString()};

        Path cupLexer = CUP_DIRECTORY.resolve("entradaCup.jflex");
        File cupLexerFile = cupLexer.toFile();
        if (cupLexerFile.exists()) {
            JFlex.Main.generate(cupLexerFile);
        }

        java_cup.Main.main(cupArguments);

        moveGeneratedFile("sym.java", CUP_DIRECTORY);
        moveGeneratedFile("Syntactic.java", CUP_DIRECTORY);
    }

    private static void moveGeneratedFile(String fileName, Path destinationDirectory) throws IOException {
        Path generatedFile = PROJECT_ROOT.resolve(fileName);

        if (!Files.exists(generatedFile)) {
            return;
        }

        Path destination = destinationDirectory.resolve(fileName);
        if (Files.exists(destination)) {
            Files.delete(destination);
        }

        Files.move(generatedFile, destination);
    }
}
