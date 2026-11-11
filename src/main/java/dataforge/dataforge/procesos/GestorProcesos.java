
package dataforge.dataforge.procesos;

import dataforge.dataforge.modelo.Ejecucion;
import dataforge.dataforge.modelo.EstadoProceso;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gestiona los procesos externos de DataForge.
 *
 * Permite ejecutar PrimeBenchmark y TextAnalyzer
 * de forma individual, secuencial, paralela y mediante Pipeline.
 *
 * También permite recuperar las salidas estándar y de error
 * de los procesos que se ejecutan de forma asíncrona.
 */
public class GestorProcesos {

    // =====================================
    // ATRIBUTOS
    // =====================================

    // Guarda los archivos temporales de cada ejecución.
    // Posición 0: stdout.
    // Posición 1: stderr.
    private final Map<Integer, Path[]> archivosEjecucion =
            new HashMap<>();

    // Identificador del siguiente proceso.
    private int siguienteId = 1;

    // =====================================
    // PRIME BENCHMARK: EJECUCIÓN SÍNCRONA
    // =====================================

    /**
     * Ejecuta PrimeBenchmark y espera a que termine.
     *
     * @param limite límite para calcular los números primos
     * @return ejecución con sus resultados
     */
    public Ejecucion ejecutarPrimeBenchmark(int limite) {

        Ejecucion ejecucion = new Ejecucion(
                siguienteId,
                "PrimeBenchmark",
                "Individual",
                String.valueOf(limite)
        );

        siguienteId++;

        ProcessBuilder constructor = crearConstructor(limite);

        ejecutarSincrono(ejecucion, constructor, null);

        return ejecucion;
    }

    /**
     * Ejecuta varios PrimeBenchmark secuencialmente.
     *
     * Cada proceso comienza cuando termina el anterior.
     *
     * @param cantidad número de ejecuciones
     * @param limite límite de cada ejecución
     * @return lista de ejecuciones
     */
    public List<Ejecucion> ejecutarPrimeBenchmarkSecuencial(
            int cantidad, int limite) {

        List<Ejecucion> ejecuciones = new ArrayList<>();

        if (cantidad > 0) {

            for (int i = 0; i < cantidad; i++) {

                Ejecucion ejecucion = ejecutarPrimeBenchmark(limite);

                ejecuciones.add(ejecucion);
            }
        }

        return ejecuciones;
    }

    /**
     * Ejecuta varios PrimeBenchmark en paralelo.
     *
     * Primero inicia todos los procesos y después
     * recupera los resultados de cada uno.
     *
     * @param cantidad número de procesos
     * @param limite límite de cada proceso
     * @return lista de ejecuciones
     */
    public List<Ejecucion> ejecutarPrimeBenchmarkParalelo(
            int cantidad, int limite) {

        List<Ejecucion> ejecuciones =
                iniciarPrimeBenchmarkParalelo(cantidad, limite);

        // Esperamos y recuperamos los resultados.
        for (Ejecucion ejecucion : ejecuciones) {

            try {

                Process proceso = ejecucion.getProceso();

                if (proceso != null) {

                    proceso.waitFor();

                    recogerResultado(ejecucion);
                }

            } catch (IOException e) {

                ejecucion.registrarError(
                        "Error al recuperar resultados: "
                                + e.getMessage()
                );

            } catch (InterruptedException e) {

                if (ejecucion.getProceso() != null) {
                    ejecucion.getProceso().destroy();
                }

                ejecucion.setStderr(
                        "La ejecución fue interrumpida."
                );

                ejecucion.registrarCancelacion();
            }
        }

        return ejecuciones;
    }

    // =====================================
    // PRIME BENCHMARK: EJECUCIÓN ASÍNCRONA
    // =====================================

    /**
     * Inicia un PrimeBenchmark individual sin esperar a que termine.
     *
     * @param limite límite para calcular los números primos
     * @return ejecución iniciada
     */
    public Ejecucion iniciarPrimeBenchmark(int limite) {

        Ejecucion ejecucion = iniciarPrimeBenchmark(
                limite,
                "Individual"
        );

        return ejecucion;
    }

    /**
     * Inicia PrimeBenchmark sin esperar a que termine.
     *
     * Guarda los archivos temporales para recuperar
     * stdout y stderr posteriormente.
     *
     * @param limite límite para calcular los números primos
     * @param modo tipo de ejecución
     * @return ejecución iniciada
     */
    public Ejecucion iniciarPrimeBenchmark(int limite, String modo) {

        Ejecucion ejecucion = new Ejecucion(
                siguienteId,
                "PrimeBenchmark",
                modo,
                String.valueOf(limite)
        );

        siguienteId++;

        Path archivoSalida = null;
        Path archivoError = null;

        try {

            // Creamos los archivos temporales.
            archivoSalida = Files.createTempFile(
                    "dataforge-salida-",
                    ".txt"
            );

            archivoError = Files.createTempFile(
                    "dataforge-error-",
                    ".txt"
            );

            // Construimos el comando.
            ProcessBuilder constructor = crearConstructor(limite);

            // Redirigimos las salidas.
            constructor.redirectOutput(archivoSalida.toFile());
            constructor.redirectError(archivoError.toFile());

            // Iniciamos el proceso.
            Process proceso = constructor.start();

            // Registramos el PID y la fecha de inicio.
            ejecucion.registrarInicio(proceso);

            // Guardamos las rutas para recuperar los resultados.
            archivosEjecucion.put(
                    ejecucion.getId(),
                    new Path[]{archivoSalida, archivoError}
            );

        } catch (IOException e) {

            ejecucion.registrarError(
                    "Error al iniciar PrimeBenchmark: "
                            + e.getMessage()
            );

            eliminarArchivo(archivoSalida);
            eliminarArchivo(archivoError);
        }

        return ejecucion;
    }

    /**
     * Inicia varios PrimeBenchmark sin esperar a que terminen.
     *
     * @param cantidad número de procesos
     * @param limite límite de cada proceso
     * @return lista de procesos iniciados
     */
    public List<Ejecucion> iniciarPrimeBenchmarkParalelo(
            int cantidad, int limite) {

        List<Ejecucion> ejecuciones = new ArrayList<>();

        if (cantidad > 0) {

            for (int i = 0; i < cantidad; i++) {

                Ejecucion ejecucion = iniciarPrimeBenchmark(
                        limite,
                        "Paralelo"
                );

                ejecuciones.add(ejecucion);
            }
        }

        return ejecuciones;
    }

    // =====================================
    // RECUPERAR RESULTADOS
    // =====================================

    /**
     * Recupera stdout, stderr y el código de salida
     * de un proceso que ya ha terminado.
     *
     * @param ejecucion ejecución que se quiere consultar
     */
    public void recogerResultado(Ejecucion ejecucion)
            throws IOException, InterruptedException {

        Process proceso = ejecucion.getProceso();

        if (proceso != null && !proceso.isAlive()) {

            // Obtenemos el código de salida.
            int codigoSalida = proceso.waitFor();

            // Buscamos los archivos asociados a la ejecución.
            Path[] archivos = archivosEjecucion.get(
                    ejecucion.getId()
            );

            if (archivos != null) {

                // Leemos stdout.
                String salida = Files.readString(
                        archivos[0],
                        StandardCharsets.UTF_8
                );

                // Leemos stderr.
                String error = Files.readString(
                        archivos[1],
                        StandardCharsets.UTF_8
                );

                // Guardamos los resultados.
                ejecucion.setStdout(salida);
                ejecucion.setStderr(error);

                // No modificamos el estado si fue cancelado.
                if (ejecucion.getEstado() != EstadoProceso.CANCELADO) {

                    ejecucion.registrarFin(codigoSalida);
                }

                // Eliminamos los archivos temporales.
                eliminarArchivo(archivos[0]);
                eliminarArchivo(archivos[1]);

                // Eliminamos la referencia del mapa.
                archivosEjecucion.remove(ejecucion.getId());
            }
        }
    }

    // =====================================
    // TEXT ANALYZER: MODO FICHERO
    // =====================================

    /**
     * Ejecuta TextAnalyzer utilizando un archivo de texto.
     *
     * @param fichero ruta del archivo
     * @param palabra palabra que se quiere buscar
     * @return ejecución con los resultados
     */
    public Ejecucion ejecutarTextAnalyzerFichero(
            Path fichero, String palabra) {

        Ejecucion ejecucion = new Ejecucion(
                siguienteId,
                "TextAnalyzer",
                "Fichero",
                fichero + " " + palabra
        );

        siguienteId++;

        // Construimos el comando.
        ProcessBuilder constructor = crearConstructorPrograma(
                "TextAnalyzer",
                fichero.toAbsolutePath().toString(),
                palabra
        );

        ejecutarSincrono(ejecucion, constructor, null);

        return ejecucion;
    }

    // =====================================
    // TEXT ANALYZER: MODO STDIN
    // =====================================

    /**
     * Ejecuta TextAnalyzer utilizando la entrada estándar.
     *
     * El texto se envía mediante stdin y la palabra
     * se pasa como argumento del programa.
     *
     * @param texto contenido que se quiere analizar
     * @param palabra palabra que se quiere buscar
     * @return ejecución con los resultados
     */
    public Ejecucion ejecutarTextAnalyzerStdin(
            String texto, String palabra) {

        Ejecucion ejecucion = new Ejecucion(
                siguienteId,
                "TextAnalyzer",
                "STDIN",
                palabra
        );

        siguienteId++;

        // Construimos el comando.
        ProcessBuilder constructor = crearConstructorPrograma(
                "TextAnalyzer",
                palabra
        );

        ejecutarSincrono(ejecucion, constructor, texto);

        return ejecucion;
    }

    // =====================================
    // EJECUCIÓN SÍNCRONA COMÚN
    // =====================================

    /**
     * Ejecuta un proceso, espera a que termine y recupera
     * sus resultados.
     *
     * Se utiliza para las ejecuciones síncronas.
     *
     * @param ejecucion registro del proceso
     * @param constructor constructor del proceso
     * @param texto texto que se enviará por stdin
     */
    private void ejecutarSincrono(
            Ejecucion ejecucion,
            ProcessBuilder constructor,
            String texto) {

        Path archivoSalida = null;
        Path archivoError = null;
        Process proceso = null;

        try {

            // Creamos los archivos temporales.
            archivoSalida = Files.createTempFile(
                    "dataforge-salida-",
                    ".txt"
            );

            archivoError = Files.createTempFile(
                    "dataforge-error-",
                    ".txt"
            );

            // Redirigimos stdout y stderr.
            constructor.redirectOutput(archivoSalida.toFile());
            constructor.redirectError(archivoError.toFile());

            // Iniciamos el proceso.
            proceso = constructor.start();

            // Registramos el inicio y el PID.
            ejecucion.registrarInicio(proceso);

            // Enviamos el texto cuando se utiliza stdin.
            try (OutputStream entrada = proceso.getOutputStream()) {

                if (texto != null) {

                    entrada.write(
                            texto.getBytes(StandardCharsets.UTF_8)
                    );
                }
            }

            // Esperamos a que termine.
            int codigoSalida = proceso.waitFor();

            // Recuperamos stdout.
            String salida = Files.readString(
                    archivoSalida,
                    StandardCharsets.UTF_8
            );

            // Recuperamos stderr.
            String error = Files.readString(
                    archivoError,
                    StandardCharsets.UTF_8
            );

            // Guardamos las salidas.
            ejecucion.setStdout(salida);
            ejecucion.setStderr(error);

            // Registramos el resultado.
            ejecucion.registrarFin(codigoSalida);

        } catch (IOException e) {

            ejecucion.registrarError(
                    "Error al ejecutar el programa: "
                            + e.getMessage()
            );

            if (proceso != null && proceso.isAlive()) {
                proceso.destroy();
            }

        } catch (InterruptedException e) {

            if (proceso != null) {
                proceso.destroy();
            }

            ejecucion.setStderr(
                    "La ejecución fue interrumpida."
            );

            ejecucion.registrarCancelacion();
        }

        // Eliminamos los archivos temporales.
        eliminarArchivo(archivoSalida);
        eliminarArchivo(archivoError);
    }

    // =====================================
    // PIPELINE ASÍNCRONO
    // =====================================

    /**
     * Inicia PrimeBenchmark y conecta su salida estándar
     * con la entrada estándar de TextAnalyzer.
     *
     * Utiliza ProcessBuilder.startPipeline().
     *
     * No espera a que terminen los procesos.
     *
     * @param limite límite de PrimeBenchmark
     * @param palabra palabra que buscará TextAnalyzer
     * @return lista con las dos ejecuciones
     */
    public List<Ejecucion> iniciarPipeline(
            int limite, String palabra) {

        List<Ejecucion> ejecuciones = new ArrayList<>();

        Path archivoSalidaPrime = null;
        Path archivoErrorPrime = null;

        Path archivoSalidaText = null;
        Path archivoErrorText = null;

        try {

            // Creamos los archivos temporales.
            archivoSalidaPrime = Files.createTempFile(
                    "dataforge-pipeline-prime-salida-",
                    ".txt"
            );

            archivoErrorPrime = Files.createTempFile(
                    "dataforge-pipeline-prime-error-",
                    ".txt"
            );

            archivoSalidaText = Files.createTempFile(
                    "dataforge-pipeline-text-salida-",
                    ".txt"
            );

            archivoErrorText = Files.createTempFile(
                    "dataforge-pipeline-text-error-",
                    ".txt"
            );

            // Creamos el registro de PrimeBenchmark.
            Ejecucion prime = new Ejecucion(
                    siguienteId,
                    "PrimeBenchmark",
                    "Pipeline",
                    String.valueOf(limite)
            );

            siguienteId++;

            // Creamos el registro de TextAnalyzer.
            Ejecucion text = new Ejecucion(
                    siguienteId,
                    "TextAnalyzer",
                    "Pipeline",
                    palabra
            );

            siguienteId++;

            ejecuciones.add(prime);
            ejecuciones.add(text);

            // Construimos el primer proceso.
            ProcessBuilder constructorPrime =
                    crearConstructorPrograma(
                            "PrimeBenchmark",
                            String.valueOf(limite)
                    );

            // Construimos el segundo proceso.
            ProcessBuilder constructorText =
                    crearConstructorPrograma(
                            "TextAnalyzer",
                            palabra
                    );

            // Capturamos los errores de PrimeBenchmark.
            constructorPrime.redirectError(
                    archivoErrorPrime.toFile()
            );

            // Capturamos los errores de TextAnalyzer.
            constructorText.redirectError(
                    archivoErrorText.toFile()
            );

            // Capturamos la salida final de TextAnalyzer.
            constructorText.redirectOutput(
                    archivoSalidaText.toFile()
            );

            // Conectamos los dos procesos.
            List<Process> procesos = ProcessBuilder.startPipeline(
                    List.of(
                            constructorPrime,
                            constructorText
                    )
            );

            // Registramos los dos procesos.
            prime.registrarInicio(procesos.get(0));
            text.registrarInicio(procesos.get(1));

            // Guardamos las rutas de los archivos.
            archivosEjecucion.put(
                    prime.getId(),
                    new Path[]{
                            archivoSalidaPrime,
                            archivoErrorPrime
                    }
            );

            archivosEjecucion.put(
                    text.getId(),
                    new Path[]{
                            archivoSalidaText,
                            archivoErrorText
                    }
            );

        } catch (IOException e) {

            // Registramos el error en las ejecuciones.
            for (Ejecucion ejecucion : ejecuciones) {

                ejecucion.registrarError(
                        "Error al iniciar el Pipeline: "
                                + e.getMessage()
                );
            }

            // Eliminamos los archivos temporales.
            eliminarArchivo(archivoSalidaPrime);
            eliminarArchivo(archivoErrorPrime);
            eliminarArchivo(archivoSalidaText);
            eliminarArchivo(archivoErrorText);
        }

        return ejecuciones;
    }

    // =====================================
    // PIPELINE SÍNCRONO
    // =====================================

    /**
     * Ejecuta el Pipeline y espera a que terminen
     * PrimeBenchmark y TextAnalyzer.
     *
     * @param limite límite de PrimeBenchmark
     * @param palabra palabra que buscará TextAnalyzer
     * @return lista de ejecuciones finalizadas
     */
    public List<Ejecucion> ejecutarPipeline(
            int limite, String palabra) {

        List<Ejecucion> ejecuciones = iniciarPipeline(
                limite,
                palabra
        );

        // Esperamos a que terminen los dos procesos.
        for (Ejecucion ejecucion : ejecuciones) {

            try {

                Process proceso = ejecucion.getProceso();

                if (proceso != null) {

                    proceso.waitFor();

                    recogerResultado(ejecucion);
                }

            } catch (IOException e) {

                ejecucion.registrarError(
                        "Error al recuperar el Pipeline: "
                                + e.getMessage()
                );

            } catch (InterruptedException e) {

                if (ejecucion.getProceso() != null) {
                    ejecucion.getProceso().destroy();
                }

                ejecucion.setStderr(
                        "El Pipeline fue interrumpido."
                );

                ejecucion.registrarCancelacion();
            }
        }

        return ejecuciones;
    }

    // =====================================
    // CONSTRUIR COMANDO PRIME BENCHMARK
    // =====================================

    /**
     * Construye el comando para ejecutar PrimeBenchmark.
     *
     * @param limite límite de cálculo
     * @return constructor del proceso
     */
    private ProcessBuilder crearConstructor(int limite) {

        ProcessBuilder constructor = crearConstructorPrograma(
                "PrimeBenchmark",
                String.valueOf(limite)
        );

        return constructor;
    }

    // =====================================
    // CONSTRUIR COMANDO DE UN PROGRAMA
    // =====================================

    /**
     * Construye el comando para ejecutar una clase
     * independiente del proyecto.
     *
     * @param clase nombre de la clase
     * @param argumentos argumentos del programa
     * @return constructor del proceso
     */
    private ProcessBuilder crearConstructorPrograma(
            String clase, String... argumentos) {

        // Ruta del ejecutable Java.
        String ejecutableJava = System.getProperty("java.home")
                + File.separator
                + "bin"
                + File.separator
                + (File.separator.equals("\\")
                ? "java.exe"
                : "java");

        // Obtenemos la ruta de los módulos.
        String rutaModulos = obtenerRutaModulos();

        // Creamos la lista del comando.
        List<String> comando = new ArrayList<>();

        comando.add(ejecutableJava);
        comando.add("--module-path");
        comando.add(rutaModulos);
        comando.add("-m");

        // Nombre del módulo y de la clase.
        comando.add(
                "dataforge.dataforge/dataforge.dataforge.programas."
                        + clase
        );

        // Añadimos los argumentos.
        for (String argumento : argumentos) {

            comando.add(argumento);
        }

        ProcessBuilder constructor = new ProcessBuilder(comando);

        return constructor;
    }

    // =====================================
    // OBTENER RUTA DE LOS MÓDULOS
    // =====================================

    /**
     * Obtiene las rutas de los módulos cargados
     * por la aplicación Java.
     *
     * @return rutas separadas según el sistema operativo
     */
    private String obtenerRutaModulos() {

        Set<String> rutas = new LinkedHashSet<>();

        // Recorremos los módulos de la aplicación.
        for (var modulo : ModuleLayer.boot().configuration().modules()) {

            var ubicacion = modulo.reference().location();

            if (ubicacion.isPresent()) {

                URI uri = ubicacion.get();

                if (uri.getScheme().equals("file")) {

                    rutas.add(
                            Path.of(uri).toString()
                    );
                }
            }
        }

        return String.join(
                File.pathSeparator,
                rutas
        );
    }

    // =====================================
    // ELIMINAR ARCHIVOS TEMPORALES
    // =====================================

    /**
     * Elimina un archivo temporal si existe.
     *
     * @param archivo ruta del archivo
     */
    private void eliminarArchivo(Path archivo) {

        if (archivo != null) {

            try {

                Files.deleteIfExists(archivo);

            } catch (IOException e) {

                System.err.println(
                        "No se pudo eliminar el archivo temporal: "
                                + e.getMessage()
                );
            }
        }
    }
}
