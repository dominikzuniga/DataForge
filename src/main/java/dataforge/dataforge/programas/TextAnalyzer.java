/**
 * @author Javier Mayorga
 */
package dataforge.dataforge.programas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Programa independiente que analiza un texto.
 *
 * Puede recibir el texto desde un fichero o mediante stdin.
 */
public class TextAnalyzer {

    public static void main(String[] args) {

        String texto = "";
        String palabraBuscar = "";

        try {

            // MODO FICHERO: recibe la ruta y la palabra.
            if (args.length == 2) {

                Path ruta = Path.of(args[0]);
                palabraBuscar = args[1];

                // Comprobamos que el fichero existe.
                if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) {
                    throw new IOException("El fichero no existe o no es válido.");
                }

                // Leemos el contenido del fichero.
                texto = Files.readString(ruta, StandardCharsets.UTF_8);

                // MODO STDIN: recibe la palabra y el texto por entrada estándar.
            } else if (args.length == 1) {

                palabraBuscar = args[0];

                // Leemos todo el texto recibido por stdin.
                texto = new String(
                        System.in.readAllBytes(),
                        StandardCharsets.UTF_8
                );

            } else {

                throw new IllegalArgumentException(
                        "Uso: TextAnalyzer <ruta> <palabra> o TextAnalyzer <palabra>"
                );
            }

            // Comprobamos que la palabra no esté vacía.
            if (palabraBuscar.isBlank()) {
                throw new IllegalArgumentException("La palabra no puede estar vacía.");
            }

            // Analizamos el contenido.
            analizarTexto(texto, palabraBuscar);

        } catch (IOException | IllegalArgumentException e) {

            // Los errores se muestran por stderr.
            System.err.println("Error: " + e.getMessage());

            // Código de salida distinto de cero.
            System.exit(1);
        }
    }

    /**
     * Analiza el texto y muestra sus estadísticas.
     */
    public static void analizarTexto(String texto, String palabraBuscar) {

        // Contamos los caracteres.
        int caracteres = texto.length();

        // Contamos las líneas.
        int lineas = 0;

        if (!texto.isEmpty()) {
            lineas = texto.split("\\R", -1).length;

            // Si termina en salto de línea, no contamos la última línea vacía.
            if (texto.endsWith("\n") || texto.endsWith("\r")) {
                lineas--;
            }
        }

        // Separamos las palabras utilizando los espacios y signos de puntuación.
        String[] palabras = texto.split("[^\\p{L}\\p{N}]+");

        int totalPalabras = 0;
        int apariciones = 0;
        int longitudMaxima = 0;
        String palabraMasLarga = "";

        // Recorremos todas las palabras.
        for (String palabra : palabras) {

            if (!palabra.isEmpty()) {

                totalPalabras++;

                // Comprobamos cuál es la palabra más larga.
                if (palabra.length() > longitudMaxima) {
                    longitudMaxima = palabra.length();
                    palabraMasLarga = palabra;
                }

                // Buscamos coincidencias sin distinguir mayúsculas.
                if (palabra.equalsIgnoreCase(palabraBuscar)) {
                    apariciones++;
                }
            }
        }

        // Mostramos los resultados por stdout.
        System.out.println("===== RESULTADO DEL ANALISIS =====");
        System.out.println("Lineas: " + lineas);
        System.out.println("Palabras: " + totalPalabras);
        System.out.println("Caracteres: " + caracteres);
        System.out.println("Palabra mas larga: " + palabraMasLarga);
        System.out.println("Apariciones de '" + palabraBuscar + "': " + apariciones);
    }
}
