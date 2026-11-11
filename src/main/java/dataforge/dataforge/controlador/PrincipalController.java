
package dataforge.dataforge.controlador;

import dataforge.dataforge.modelo.Ejecucion;
import dataforge.dataforge.modelo.EstadoProceso;
import dataforge.dataforge.procesos.GestorProcesos;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal de DataForge.
 *
 * Gestiona la interfaz gráfica y las ejecuciones
 * de los programas externos.
 */
public class PrincipalController {

    // Gestor encargado de iniciar y controlar los procesos.
    private GestorProcesos gestorProcesos = new GestorProcesos();

    // Lista que contiene todas las ejecuciones.
    private ObservableList<Ejecucion> listaEjecuciones;

    // Archivo seleccionado para TextAnalyzer.
    private File archivoSeleccionado;

    // =====================================
    // EJECUCIÓN INDIVIDUAL
    // =====================================

    private Timeline comprobadorProceso;

    private Ejecucion ejecucionActual;

    private long tiempoInicio;

    // =====================================
    // EJECUCIÓN SECUENCIAL Y PARALELA
    // =====================================

    private Timeline comprobadorGrupo;

    private List<Ejecucion> ejecucionesGrupo = new ArrayList<>();

    private int limiteGrupo;
    private int cantidadGrupo;
    private int indiceSecuencial;

    private boolean modoParalelo;

    private long tiempoInicioGrupo;

    // =====================================
    // CAMPOS DE CONFIGURACIÓN
    // =====================================

    @FXML
    private TextField campoLimite;

    @FXML
    private TextField campoCantidad;

    @FXML
    private TextField campoPalabra;

    @FXML
    private TextField campoRutaArchivo;

    @FXML
    private TextArea campoTextoEntrada;

    // =====================================
    // BOTONES
    // =====================================

    @FXML
    private Button btnIndividual;

    @FXML
    private Button btnSecuencial;

    @FXML
    private Button btnParalelo;

    @FXML
    private Button btnPipeline;

    @FXML
    private Button btnDetener;

    // =====================================
    // SELECTORES DE REDIRECCIÓN
    // =====================================

    @FXML
    private ComboBox<String> comboStdout;

    @FXML
    private ComboBox<String> comboStderr;

    // =====================================
    // TABLA DE EJECUCIONES
    // =====================================

    @FXML
    private TableView<Ejecucion> tablaProcesos;

    @FXML
    private TableColumn<Ejecucion, Integer> colId;

    @FXML
    private TableColumn<Ejecucion, Long> colPid;

    @FXML
    private TableColumn<Ejecucion, String> colTarea;

    @FXML
    private TableColumn<Ejecucion, String> colModo;

    @FXML
    private TableColumn<Ejecucion, String> colEstado;

    @FXML
    private TableColumn<Ejecucion, String> colInicio;

    @FXML
    private TableColumn<Ejecucion, String> colFin;

    @FXML
    private TableColumn<Ejecucion, String> colDuracion;

    @FXML
    private TableColumn<Ejecucion, String> colCodigo;

    // =====================================
    // ÁREAS DE RESULTADOS
    // =====================================

    @FXML
    private TextArea areaStdout;

    @FXML
    private TextArea areaStderr;

    @FXML
    private Label etiquetaTiempoTotal;

    // =====================================
    // INICIALIZACIÓN
    // =====================================

    /**
     * Inicializa los componentes de la interfaz.
     */
    @FXML
    public void initialize() {

        // Creamos la lista de ejecuciones.
        listaEjecuciones = FXCollections.observableArrayList();

        // Conectamos la lista con la tabla.
        tablaProcesos.setItems(listaEjecuciones);

        // Configuramos las columnas.
        configurarTabla();

        // Configuramos los selectores de redirección.
        configurarSelectoresRedireccion();

        // Detectamos cuándo se selecciona una fila.
        tablaProcesos.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> {
                    mostrarResultadoSeleccionado(seleccionada);
                }
        );

        // Mensajes iniciales.
        areaStdout.setText(
                "Selecciona un proceso para consultar su salida."
        );

        areaStderr.setText(
                "Selecciona un proceso para consultar sus errores."
        );
    }

    /**
     * Configura las opciones disponibles para stdout y stderr.
     */
    private void configurarSelectoresRedireccion() {

        ObservableList<String> opciones =
                FXCollections.observableArrayList(
                        "Capturar en DataForge",
                        "Fichero",
                        "DISCARD"
                );

        comboStdout.setItems(opciones);

        comboStderr.setItems(
                FXCollections.observableArrayList(opciones)
        );

        comboStdout.setValue("Capturar en DataForge");
        comboStderr.setValue("Capturar en DataForge");
    }

    // =====================================
    // CONFIGURACIÓN DE LA TABLA
    // =====================================

    /**
     * Asigna las propiedades de Ejecucion a cada columna.
     *
     * No se utilizan expresiones lambda.
     */
    private void configurarTabla() {

        // Identificador y PID.
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colPid.setCellValueFactory(
                new PropertyValueFactory<>("pid")
        );

        // Datos generales.
        colTarea.setCellValueFactory(
                new PropertyValueFactory<>("tarea")
        );

        colModo.setCellValueFactory(
                new PropertyValueFactory<>("modo")
        );

        // Propiedades auxiliares de tipo String.
        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estadoTexto")
        );

        colInicio.setCellValueFactory(
                new PropertyValueFactory<>("inicioTexto")
        );

        colFin.setCellValueFactory(
                new PropertyValueFactory<>("finTexto")
        );

        colDuracion.setCellValueFactory(
                new PropertyValueFactory<>("duracionTexto")
        );

        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigoSalidaTexto")
        );

        // Anchura de las columnas.
        colId.setPrefWidth(45);
        colPid.setPrefWidth(75);
        colTarea.setPrefWidth(100);
        colModo.setPrefWidth(70);
        colEstado.setPrefWidth(90);
        colInicio.setPrefWidth(85);
        colFin.setPrefWidth(85);
        colDuracion.setPrefWidth(85);
        colCodigo.setPrefWidth(75);

        // Anchura mínima para evitar que desaparezcan.
        colId.setMinWidth(40);
        colPid.setMinWidth(65);
        colTarea.setMinWidth(75);
        colModo.setMinWidth(55);
        colEstado.setMinWidth(75);
        colInicio.setMinWidth(70);
        colFin.setMinWidth(70);
        colDuracion.setMinWidth(70);
        colCodigo.setMinWidth(60);

        // Permite que las columnas mantengan su tamaño preferido.
        tablaProcesos.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );
    }

    // =====================================
    // AÑADIR EJECUCIONES A LA TABLA
    // =====================================

    /**
     * Añade una ejecución a la tabla.
     */
    public void agregarEjecucion(Ejecucion ejecucion) {

        listaEjecuciones.add(ejecucion);

    }

    /**
     * Añade varias ejecuciones a la tabla.
     */
    public void agregarEjecuciones(List<Ejecucion> ejecuciones) {
        listaEjecuciones.addAll(ejecuciones);
    }

    // =====================================
    // MOSTRAR RESULTADOS
    // =====================================

    /**
     * Muestra stdout y stderr de la ejecución seleccionada.
     */
    private void mostrarResultadoSeleccionado(Ejecucion ejecucion) {

        if (ejecucion == null) {

            areaStdout.setText(
                    "Selecciona un proceso para ver su salida."
            );

            areaStderr.setText("");

        } else if (ejecucion.getModo().equals("Pipeline")
                && ejecucion.getTarea().equals("PrimeBenchmark")) {

            areaStdout.setText(
                    "===== PIPELINE =====\n\n"
                            + "Proceso: PrimeBenchmark\n\n"
                            + "La salida estándar de este proceso está conectada "
                            + "mediante una tubería a TextAnalyzer.\n\n"
                            + "Para consultar el resultado final, selecciona "
                            + "la fila de TextAnalyzer."
            );

            areaStderr.setText(ejecucion.getStderr());

        } else {

            areaStdout.setText(ejecucion.getStdout());
            areaStderr.setText(ejecucion.getStderr());
        }
    }

    // =====================================
    // COMPROBAR PROCESOS ACTIVOS
    // =====================================

    /**
     * Comprueba si existe una supervisión activa.
     */
    private boolean hayComprobacionesActivas() {

        boolean individualActivo = comprobadorProceso != null
                && comprobadorProceso.getStatus() == Timeline.Status.RUNNING;

        boolean grupoActivo = comprobadorGrupo != null
                && comprobadorGrupo.getStatus() == Timeline.Status.RUNNING;

        return individualActivo || grupoActivo;
    }

    // =====================================
    // EJECUCIÓN INDIVIDUAL
    // =====================================

    /**
     * Inicia un PrimeBenchmark individual.
     */
    @FXML
    private void ejecutarIndividual() {

        try {

            int limite = Integer.parseInt(campoLimite.getText());

            if (limite < 2) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Dato incorrecto",
                        "Límite no válido",
                        "El límite debe ser igual o superior a 2."
                );

            } else if (hayComprobacionesActivas()) {

                mostrarAvisoProcesoActivo();

            } else {

                tiempoInicio = System.currentTimeMillis();

                // Iniciamos el proceso sin esperar a que termine.
                ejecucionActual =
                        gestorProcesos.iniciarPrimeBenchmark(limite);

                agregarEjecucion(ejecucionActual);

                tablaProcesos.getSelectionModel().select(ejecucionActual);

                if (ejecucionActual.getEstado() == EstadoProceso.ERROR) {

                    areaStderr.setText(ejecucionActual.getStderr());

                } else {

                    iniciarComprobacion(ejecucionActual);
                }
            }

        } catch (NumberFormatException e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Valor incorrecto",
                    "Introduce un número entero válido."
            );
        }
    }

    /**
     * Supervisa la ejecución individual mediante Timeline.
     *
     * Timeline ejecuta las comprobaciones desde el hilo de JavaFX;
     * no se crea ningún hilo adicional.
     */
    private void iniciarComprobacion(Ejecucion ejecucionSupervisada) {

        comprobadorProceso = new Timeline(
                new KeyFrame(Duration.millis(200), evento -> {

                    Process proceso = ejecucionSupervisada.getProceso();

                    if (proceso != null && !proceso.isAlive()) {

                        comprobadorProceso.stop();

                        recogerResultadoGrupo(ejecucionSupervisada);

                        long tiempoFin = System.currentTimeMillis();

                        etiquetaTiempoTotal.setText(
                                "Tiempo total: "
                                        + (tiempoFin - tiempoInicio)
                                        + " ms"
                        );
                    }
                })
        );

        comprobadorProceso.setCycleCount(Timeline.INDEFINITE);
        comprobadorProceso.play();
    }

    // =====================================
    // EJECUCIÓN SECUENCIAL
    // =====================================

    @FXML
    private void ejecutarSecuencial() {
        ejecutarGrupoProcesos(false);
    }

    // =====================================
    // EJECUCIÓN PARALELA
    // =====================================

    @FXML
    private void ejecutarParalelo() {
        ejecutarGrupoProcesos(true);
    }

    // =====================================
    // INICIAR GRUPO DE PROCESOS
    // =====================================

    /**
     * Inicia varias ejecuciones en modo secuencial o paralelo.
     *
     * @param paralelo indica el modo de ejecución
     */
    private void ejecutarGrupoProcesos(boolean paralelo) {

        try {

            int limite = Integer.parseInt(campoLimite.getText());
            int cantidad = Integer.parseInt(campoCantidad.getText());

            if (limite < 2 || cantidad < 1) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Datos incorrectos",
                        "Valores no válidos",
                        "El límite debe ser igual o superior a 2 "
                                + "y la cantidad debe ser superior a 0."
                );

            } else if (hayComprobacionesActivas()) {

                mostrarAvisoProcesoActivo();

            } else {

                limiteGrupo = limite;
                cantidadGrupo = cantidad;
                modoParalelo = paralelo;
                indiceSecuencial = 0;

                ejecucionesGrupo.clear();

                tiempoInicioGrupo = System.currentTimeMillis();

                if (modoParalelo) {

                    // Iniciamos todos los procesos.
                    ejecucionesGrupo =
                            gestorProcesos.iniciarPrimeBenchmarkParalelo(
                                    cantidadGrupo,
                                    limiteGrupo
                            );

                    agregarEjecuciones(ejecucionesGrupo);

                } else {

                    // Iniciamos solamente el primero.
                    iniciarSiguienteSecuencial();
                }

                if (!ejecucionesGrupo.isEmpty()) {

                    tablaProcesos.getSelectionModel().select(
                            ejecucionesGrupo.get(ejecucionesGrupo.size() - 1)
                    );
                }

                iniciarComprobacionGrupo();
            }

        } catch (NumberFormatException e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Valores incorrectos",
                    "Introduce números enteros válidos en los campos."
            );
        }
    }

    /**
     * Inicia el siguiente proceso del grupo secuencial.
     */
    private void iniciarSiguienteSecuencial() {

        Ejecucion ejecucion = gestorProcesos.iniciarPrimeBenchmark(
                limiteGrupo,
                "Secuencial"
        );

        ejecucionesGrupo.add(ejecucion);

        agregarEjecucion(ejecucion);

        indiceSecuencial++;

        tablaProcesos.getSelectionModel().select(ejecucion);
    }

    // =====================================
    // COMPROBACIÓN DEL GRUPO
    // =====================================

    /**
     * Inicia la supervisión del grupo de procesos.
     */
    private void iniciarComprobacionGrupo() {

        comprobadorGrupo = new Timeline(
                new KeyFrame(Duration.millis(200), evento -> {

                    if (modoParalelo) {

                        comprobarProcesosParalelos();

                    } else {

                        comprobarProcesoSecuencial();
                    }
                })
        );

        comprobadorGrupo.setCycleCount(Timeline.INDEFINITE);
        comprobadorGrupo.play();
    }

    /**
     * Comprueba las ejecuciones paralelas.
     */
    private void comprobarProcesosParalelos() {

        boolean todosFinalizados = true;

        for (Ejecucion ejecucion : ejecucionesGrupo) {

            Process proceso = ejecucion.getProceso();

            if (proceso != null && !proceso.isAlive()) {

                if (ejecucion.getEstado() == EstadoProceso.EJECUTANDO
                        || ejecucion.getEstado() == EstadoProceso.CANCELADO) {

                    recogerResultadoGrupo(ejecucion);
                }
            }

            if (ejecucion.getEstado() == EstadoProceso.EJECUTANDO) {
                todosFinalizados = false;
            }
        }

        if (todosFinalizados) {
            finalizarGrupo();
        }
    }

    /**
     * Comprueba la ejecución secuencial actual.
     */
    private void comprobarProcesoSecuencial() {

        if (!ejecucionesGrupo.isEmpty()) {

            Ejecucion ejecucion = ejecucionesGrupo.get(
                    ejecucionesGrupo.size() - 1
            );

            Process proceso = ejecucion.getProceso();

            if (proceso == null || !proceso.isAlive()) {

                if (proceso != null) {

                    if (ejecucion.getEstado() == EstadoProceso.EJECUTANDO
                            || ejecucion.getEstado() == EstadoProceso.CANCELADO) {

                        recogerResultadoGrupo(ejecucion);
                    }
                }

                if (indiceSecuencial < cantidadGrupo) {

                    iniciarSiguienteSecuencial();

                } else {

                    finalizarGrupo();
                }
            }
        }
    }

    // =====================================
    // RECUPERAR RESULTADOS
    // =====================================

    /**
     * Recupera stdout, stderr y el código de salida.
     */
    private void recogerResultadoGrupo(Ejecucion ejecucion) {

        try {

            gestorProcesos.recogerResultado(ejecucion);

        } catch (IOException | InterruptedException e) {

            ejecucion.registrarError(
                    "Error al recuperar los resultados: " + e.getMessage()
            );
        }

        tablaProcesos.refresh();

        // Actualizamos las áreas si es la fila seleccionada.
        if (tablaProcesos.getSelectionModel().getSelectedItem() == ejecucion) {

            mostrarResultadoSeleccionado(ejecucion);
        }
    }

    // =====================================
    // FINALIZAR GRUPO
    // =====================================

    /**
     * Detiene la supervisión y muestra el tiempo total.
     */
    private void finalizarGrupo() {

        comprobadorGrupo.stop();

        long tiempoFin = System.currentTimeMillis();

        etiquetaTiempoTotal.setText(
                "Tiempo total: "
                        + (tiempoFin - tiempoInicioGrupo)
                        + " ms"
        );

        tablaProcesos.refresh();
    }

    // =====================================
    // EJECUCIÓN DEL PIPELINE
    // =====================================

    /**
     * Inicia PrimeBenchmark conectado con TextAnalyzer.
     */
    @FXML
    private void ejecutarPipeline() {

        try {

            int limite = Integer.parseInt(campoLimite.getText());

            String palabra = campoPalabra.getText().trim();

            if (limite < 2 || palabra.isEmpty()) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Datos incorrectos",
                        "Valores no válidos",
                        "Introduce un límite válido y una palabra."
                );

            } else if (hayComprobacionesActivas()) {

                mostrarAvisoProcesoActivo();

            } else {

                tiempoInicio = System.currentTimeMillis();

                // Iniciamos los dos procesos conectados.
                ejecucionesGrupo = gestorProcesos.iniciarPipeline(
                        limite,
                        palabra
                );

                // Añadimos los procesos a la tabla.
                agregarEjecuciones(ejecucionesGrupo);

                // Seleccionamos el último proceso.
                if (!ejecucionesGrupo.isEmpty()) {

                    tablaProcesos.getSelectionModel().select(
                            ejecucionesGrupo.get(ejecucionesGrupo.size() - 1)
                    );
                }

                iniciarComprobacionPipeline();
            }

        } catch (NumberFormatException e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Valor incorrecto",
                    "Introduce un límite numérico válido."
            );
        }
    }

    /**
     * Comprueba cuándo terminan los dos procesos del Pipeline.
     */
    private void iniciarComprobacionPipeline() {

        comprobadorGrupo = new Timeline(
                new KeyFrame(Duration.millis(200), evento -> {

                    boolean todosFinalizados = true;

                    for (Ejecucion ejecucion : ejecucionesGrupo) {

                        Process proceso = ejecucion.getProceso();

                        if (proceso != null && proceso.isAlive()) {

                            todosFinalizados = false;

                        } else if (ejecucion.getEstado()
                                == EstadoProceso.EJECUTANDO
                                || ejecucion.getEstado()
                                == EstadoProceso.CANCELADO) {

                            recogerResultadoGrupo(ejecucion);
                        }
                    }

                    if (todosFinalizados) {

                        comprobadorGrupo.stop();

                        long tiempoFin = System.currentTimeMillis();

                        etiquetaTiempoTotal.setText(
                                "Tiempo total: "
                                        + (tiempoFin - tiempoInicio)
                                        + " ms"
                        );

                        tablaProcesos.refresh();
                    }
                })
        );

        comprobadorGrupo.setCycleCount(Timeline.INDEFINITE);
        comprobadorGrupo.play();
    }

    // =====================================
    // DETENER PROCESO
    // =====================================

    /**
     * Detiene el proceso seleccionado.
     */
    @FXML
    private void detenerProceso() {

        Ejecucion ejecucion =
                tablaProcesos.getSelectionModel().getSelectedItem();

        if (ejecucion == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Sin selección",
                    "Selecciona un proceso",
                    "Debes seleccionar un proceso de la tabla."
            );

        } else if (ejecucion.getProceso() != null
                && ejecucion.getProceso().isAlive()) {

            // Detenemos el proceso externo.
            ejecucion.getProceso().destroy();

            // Registramos la cancelación.
            ejecucion.registrarCancelacion();

            tablaProcesos.refresh();

        } else {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Proceso finalizado",
                    "No se puede detener",
                    "El proceso ya ha terminado."
            );
        }
    }

    // =====================================
    // TEXT ANALYZER: SELECCIONAR ARCHIVO
    // =====================================

    /**
     * Abre el selector de archivos TXT.
     */
    @FXML
    private void seleccionarArchivo() {

        FileChooser selector = new FileChooser();

        selector.setTitle("Seleccionar archivo de texto");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos de texto (*.txt)",
                        "*.txt"
                )
        );

        File archivo = selector.showOpenDialog(
                campoRutaArchivo.getScene().getWindow()
        );

        if (archivo != null) {

            archivoSeleccionado = archivo;

            campoRutaArchivo.setText(
                    archivoSeleccionado.getAbsolutePath()
            );
        }
    }

    // =====================================
    // TEXT ANALYZER: MODO FICHERO
    // =====================================

    /**
     * Ejecuta TextAnalyzer utilizando un archivo.
     */
    @FXML
    private void ejecutarTextAnalyzerFichero() {

        String palabra = campoPalabra.getText().trim();

        if (archivoSeleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Archivo no seleccionado",
                    "Falta seleccionar un archivo",
                    "Selecciona un archivo de texto primero."
            );

        } else if (palabra.isEmpty()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Palabra no válida",
                    "Falta la palabra de búsqueda",
                    "Introduce la palabra que quieres buscar."
            );

        } else if (hayComprobacionesActivas()) {

            mostrarAvisoProcesoActivo();

        } else {

            tiempoInicio = System.currentTimeMillis();

            // Ejecutamos TextAnalyzer con el fichero.
            Ejecucion ejecucion =
                    gestorProcesos.ejecutarTextAnalyzerFichero(
                            archivoSeleccionado.toPath(),
                            palabra
                    );

            agregarEjecucion(ejecucion);

            tablaProcesos.getSelectionModel().select(ejecucion);

            mostrarResultadoSeleccionado(ejecucion);

            long tiempoFin = System.currentTimeMillis();

            etiquetaTiempoTotal.setText(
                    "Tiempo total: "
                            + (tiempoFin - tiempoInicio)
                            + " ms"
            );

            tablaProcesos.refresh();
        }
    }

    // =====================================
    // TEXT ANALYZER: MODO STDIN
    // =====================================

    /**
     * Ejecuta TextAnalyzer enviando el texto mediante stdin.
     */
    @FXML
    private void ejecutarTextAnalyzerStdin() {

        String texto = campoTextoEntrada.getText();

        String palabra = campoPalabra.getText().trim();

        if (texto.trim().isEmpty()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Texto vacío",
                    "No hay texto para analizar",
                    "Introduce el texto que quieres analizar."
            );

        } else if (palabra.isEmpty()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Palabra no válida",
                    "Falta la palabra de búsqueda",
                    "Introduce la palabra que quieres buscar."
            );

        } else if (hayComprobacionesActivas()) {

            mostrarAvisoProcesoActivo();

        } else {

            tiempoInicio = System.currentTimeMillis();

            // Ejecutamos TextAnalyzer mediante stdin.
            Ejecucion ejecucion =
                    gestorProcesos.ejecutarTextAnalyzerStdin(
                            texto,
                            palabra
                    );

            agregarEjecucion(ejecucion);

            tablaProcesos.getSelectionModel().select(ejecucion);

            mostrarResultadoSeleccionado(ejecucion);

            long tiempoFin = System.currentTimeMillis();

            etiquetaTiempoTotal.setText(
                    "Tiempo total: "
                            + (tiempoFin - tiempoInicio)
                            + " ms"
            );

            tablaProcesos.refresh();
        }
    }

    // =====================================
    // MENSAJES Y ALERTAS
    // =====================================

    /**
     * Muestra un aviso cuando ya hay procesos activos.
     */
    private void mostrarAvisoProcesoActivo() {

        mostrarAlerta(
                Alert.AlertType.WARNING,
                "Proceso en ejecución",
                "Ya hay procesos activos",
                "Espera a que terminen antes de iniciar otra ejecución."
        );
    }

    /**
     * Muestra una ventana de alerta.
     */
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String encabezado,
            String mensaje) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
