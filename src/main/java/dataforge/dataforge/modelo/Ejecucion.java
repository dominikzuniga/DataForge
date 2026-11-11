/**
 * @author Dominik Zuniga Jarquin
 */
package dataforge.dataforge.modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una ejecución de un programa externo.
 *
 * Guarda los datos del proceso, sus tiempos, su estado,
 * el código de salida y los resultados de stdout y stderr.
 */
public class Ejecucion {

    // Identificador interno de la ejecución.
    private int id;

    // PID asignado por el sistema operativo.
    private Long pid;

    // Información del programa ejecutado.
    private String tarea;
    private String modo;
    private String parametros;

    // Estado actual del proceso.
    private EstadoProceso estado;

    // Fechas de inicio y finalización.
    private LocalDateTime inicio;
    private LocalDateTime fin;

    // Duración total en milisegundos.
    private long duracion;

    // Código devuelto por el programa.
    private Integer codigoSalida;

    // Resultados de las salidas estándar y de error.
    private String stdout;
    private String stderr;

    // Referencia al proceso externo.
    private Process proceso;

    // Formato utilizado para mostrar las horas.
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Constructor de una ejecución.
     *
     * @param id identificador de la ejecución
     * @param tarea nombre del programa
     * @param modo tipo de ejecución
     * @param parametros argumentos utilizados
     */
    public Ejecucion(int id, String tarea, String modo, String parametros) {

        this.id = id;
        this.tarea = tarea;
        this.modo = modo;
        this.parametros = parametros;

        this.estado = EstadoProceso.PENDIENTE;

        this.stdout = "";
        this.stderr = "";

        this.duracion = 0;
    }

    /**
     * Registra el inicio del proceso y obtiene su PID.
     *
     * @param proceso proceso externo iniciado
     */
    public void registrarInicio(Process proceso) {

        this.proceso = proceso;

        this.pid = proceso.pid();

        this.inicio = LocalDateTime.now();

        this.estado = EstadoProceso.EJECUTANDO;
    }

    /**
     * Registra la finalización del proceso.
     *
     * Si el código es 0, la ejecución finaliza correctamente.
     * Si es diferente de 0, se registra como error.
     *
     * @param codigoSalida código devuelto por el proceso
     */
    public void registrarFin(int codigoSalida) {

        this.codigoSalida = codigoSalida;

        this.fin = LocalDateTime.now();

        if (inicio != null) {
            this.duracion = Duration.between(inicio, fin).toMillis();
        }

        if (codigoSalida == 0) {
            this.estado = EstadoProceso.FINALIZADO;
        } else {
            this.estado = EstadoProceso.ERROR;
        }
    }

    /**
     * Registra la cancelación del proceso.
     */
    public void registrarCancelacion() {

        this.fin = LocalDateTime.now();

        if (inicio != null) {
            this.duracion = Duration.between(inicio, fin).toMillis();
        }

        this.estado = EstadoProceso.CANCELADO;
    }

    /**
     * Registra un error producido durante la ejecución.
     *
     * @param mensaje descripción del error
     */
    public void registrarError(String mensaje) {

        this.stderr = mensaje;

        this.fin = LocalDateTime.now();

        if (inicio != null) {
            this.duracion = Duration.between(inicio, fin).toMillis();
        }

        this.estado = EstadoProceso.ERROR;
    }

    // =====================================
    // GETTERS Y SETTERS
    // =====================================

    public int getId() {
        return id;
    }

    public Long getPid() {
        return pid;
    }

    public String getTarea() {
        return tarea;
    }

    public String getModo() {
        return modo;
    }

    public String getParametros() {
        return parametros;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public long getDuracion() {
        return duracion;
    }

    public Integer getCodigoSalida() {
        return codigoSalida;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public Process getProceso() {
        return proceso;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    // =====================================
    // GETTERS PARA LAS COLUMNAS DE LA TABLA
    // =====================================

    /**
     * Devuelve el estado como texto.
     */
    public String getEstadoTexto() {
        return estado == null ? "-" : estado.toString();
    }

    /**
     * Devuelve la hora de inicio con formato HH:mm:ss.
     */
    public String getInicioTexto() {
        String resultado = "-";

        if (inicio != null) {
            resultado = inicio.format(FORMATO_HORA);
        }

        return resultado;
    }

    /**
     * Devuelve la hora de finalización con formato HH:mm:ss.
     */
    public String getFinTexto() {
        String resultado = "-";

        if (fin != null) {
            resultado = fin.format(FORMATO_HORA);
        }

        return resultado;
    }

    /**
     * Devuelve la duración en milisegundos.
     */
    public String getDuracionTexto() {
        return duracion + " ms";
    }

    /**
     * Devuelve el código de salida como texto.
     */
    public String getCodigoSalidaTexto() {
        String resultado = "-";

        if (codigoSalida != null) {
            resultado = codigoSalida.toString();
        }

        return resultado;
    }
}
