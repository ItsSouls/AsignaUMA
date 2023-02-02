package es.uma.asignauma.Controlador;

import es.uma.asignauma.Modelo.BD;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controlador {
    @FXML
    private TabPane general;
    private int pestanaAnterior = 0;

    public Controlador() {

    }

    public void setAulaVigi() {
        if ((pestanaAnterior == 5 && general.getSelectionModel().getSelectedIndex() != 5 && BD.getInstance().cambio)) {
            BD.getInstance().getControladorRAula().cargar();
            BD.getInstance().cambio = false;
        } else if (pestanaAnterior == 6 && general.getSelectionModel().getSelectedIndex() != 6 && BD.getInstance().cambio) {
            BD.getInstance().getControladorVigilantes().cargar();
            BD.getInstance().cambio = false;
        }

        if (general.getSelectionModel().getSelectedIndex() == 5) {
            pestanaAnterior = 5;
            BD.getInstance().setPestana(1);
        } else if (general.getSelectionModel().getSelectedIndex() == 6) {
            pestanaAnterior = 6;
            BD.getInstance().setPestana(2);
        } else {
            pestanaAnterior = general.getSelectionModel().getSelectedIndex();
        }
    }

    private File fileChooser() {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Seleccionar archivo de usuarios");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));

        return fileChooser.showOpenDialog(fileChooserStage);
    }

    @FXML
    public void onLoadButtonClick() {
        File file = fileChooser();

        if (file != null) {
            BD.getInstance().cargarRAulaVigi(file.toString());
        }
    }
}