/**
 * @author Dominik Zuniga Jarquin
 */
package dataforge.dataforge.programas;

/**
 * Programa independiente que calcula números primos.
 *
 * Recibe un número N y cuenta cuántos números primos
 * existen entre 2 y N.
 */
public class PrimeBenchmark {

    public static void main(String[] args) {

        try {

            // Comprobamos que se haya recibido un argumento.
            if (args.length != 1) {
                throw new IllegalArgumentException(
                        "Debes indicar un numero entero."
                );
            }

            // Convertimos el argumento a entero.
            int limite = Integer.parseInt(args[0]);

            // Comprobamos que el número sea válido.
            if (limite < 2) {
                throw new IllegalArgumentException(
                        "El numero debe ser igual o mayor que 2."
                );
            }

            int totalPrimos = 0;

            // Recorremos los números desde 2 hasta N.
            for (int numero = 2; numero <= limite; numero++) {

                boolean esPrimo = true;

                // Comprobamos si el número tiene divisores.
                for (int divisor = 2; divisor <= numero / divisor; divisor++) {

                    if (numero % divisor == 0) {
                        esPrimo = false;
                    }
                }

                // Si es primo, aumentamos el contador.
                if (esPrimo) {
                    totalPrimos++;
                }
            }

            // Mostramos el resultado por stdout.
            System.out.println("===== PRIME BENCHMARK =====");
            System.out.println("Limite: " + limite);
            System.out.println("Total de numeros primos: " + totalPrimos);

        } catch ( IllegalArgumentException e) {

            // Los errores se muestran por stderr.
            System.err.println("Error: " + e.getMessage());

            // Código de salida distinto de cero.
            System.exit(1);
        }
    }
}
