module es.uma.asignauma {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;


    opens es.uma.asignauma to javafx.fxml;
    exports es.uma.asignauma;
    exports es.uma.asignauma.Controlador;
    opens es.uma.asignauma.Controlador to javafx.fxml;
    opens es.uma.asignauma.Modelo to javafx.base;
}