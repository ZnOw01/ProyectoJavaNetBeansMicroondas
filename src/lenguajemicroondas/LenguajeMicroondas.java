/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package lenguajemicroondas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author guill
 */
public class LenguajeMicroondas {
    
    /**
     * @param args the command line arguments
     */
    public static void checkParser(){
        ventana window = new ventana();
        window.setVisible(true);
    }
    public static void checkLexer(){
        ventana window = new ventana();
        window.setVisible(true);
    }
    public static void generarLexer(){
        String ruta =  "D:\\Programacion\\Java\\LenguajeMicroondas\\src\\lenguajemicroondas\\entrada.jflex";
        File archivo = new File(ruta);
        JFlex.Main.generate(archivo);
    }
    public static void main(String[] args) throws Exception {
        //generarLexer();
        generarCup();
    }
    public static void generarCup() throws IOException, Exception{
        String path0 =  "D:\\Programacion\\Java\\LenguajeMicroondas\\";
        String path = path0 +  "src\\lenguajemicroondas\\";
        String rutaC = path + "entradaCup.jflex";
        String fileG = "Syntactic.java";
        String[] rutaS = {"-parser", "Syntactic", path + "Grammar.cup"};
        File archivo;
        archivo = new File(rutaC);
        JFlex.Main.generate(archivo);
        System.out.println("Fin Lexico");
        java_cup.Main.main(rutaS);
        System.out.println("ruta---");
        
        Path rutaSym = Paths.get(path + "sym.java");
        if (Files.exists(rutaSym)){
            Files.delete(rutaSym);
        }
        Files.move(Paths.get(path0 + "sym.java"), Paths.get(path + "sym.java"));
        
        Path rutaSim = Paths.get(path + fileG);
        if(Files.exists(rutaSim)){
            Files.delete(rutaSim);
        }
        Files.move(Paths.get(path0 + fileG), Paths.get(path + fileG));
    }
}
