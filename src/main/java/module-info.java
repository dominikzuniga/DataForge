module dataforge.dataforge {
    requires javafx.controls;
    requires javafx.fxml;

    opens dataforge.dataforge to javafx.fxml;
    opens dataforge.dataforge.controlador to javafx.fxml;
    opens dataforge.dataforge.modelo to javafx.base;

    exports dataforge.dataforge;
    exports dataforge.dataforge.programas;
}