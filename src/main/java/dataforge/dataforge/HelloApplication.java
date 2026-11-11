
package dataforge.dataforge;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicacion DataForge.
 * Se encarga de iniciar JavaFX y mostrar la ventana principal.
 */
public class HelloApplication extends Application {

    /**
     * Metodo que inicia la ventana principal.
     *
     * @param stage escenario principal de JavaFX
     * @throws IOException si no se puede cargar el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {

        // Cargamos la interfaz principal
        FXMLLoader cargador = new FXMLLoader(
                HelloApplication.class.getResource(
                        "vista/principal.fxml"
                )
        );

        // Creamos la escena con la interfaz cargada
        Scene escena = new Scene(cargador.load());

        // Configuramos la ventana
        stage.setTitle("DataForge - Motor visual de procesos");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setWidth(1200);
        stage.setHeight(750);

        // Mostramos la ventana
        stage.setScene(escena);
        stage.show();
    }
}
