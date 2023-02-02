package es.uma.asignauma.Controlador;

import es.uma.asignauma.Modelo.BD;
import es.uma.asignauma.Modelo.BDException;
import es.uma.asignauma.Modelo.ResponsableSede;
import es.uma.asignauma.Modelo.Sede;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CtrSedes implements Initializable {
    @FXML
    private TableView<Sede> tablaSedes;
    @FXML
    private TableColumn<Sede, String> columnaSede;
    @FXML
    private TableColumn<Sede, Integer> columnaAforo;
    @FXML
    private TableColumn<Sede, ComboBox<ResponsableSede>> columnaResponsable;
    @FXML
    private TextField buscarSede;
    private ObservableList<Sede> sedes;

    public void cargar() {
        BD.getInstance().cargarRespSedes();
        sedes = FXCollections.observableArrayList(BD.getInstance().leerSedes());
        columnaSede.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getNombre()));
        columnaResponsable.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getRSedeBox()));
        columnaAforo.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getAforo()));

        tablaSedes.setItems(sedes);
    }

    private void sinSeleccion(String titulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText("No ha seleccionado ninguna sede");

        alert.showAndWait();
    }

    @FXML
    public void onSearchButtonClick() {
        FilteredList<Sede> sedeFiltrada = new FilteredList<>(sedes, b -> true);
        String palabraBuscada = buscarSede.getText().toLowerCase();

        sedeFiltrada.setPredicate(sede -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            if (sede.getNombre().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (sede.getRSedeAsignado() != null) {
                return sede.getRSedeAsignado().getNombre().toLowerCase().contains(palabraBuscada);
            }

            return false;
        });

        SortedList<Sede> sedesOrdenadas = new SortedList<>(sedeFiltrada);

        sedesOrdenadas.comparatorProperty().bind(tablaSedes.comparatorProperty());

        tablaSedes.setItems(sedesOrdenadas);
    }

    @FXML
    protected void onEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSearchButtonClick();
        }
    }
    private File fileChooser() {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Seleccionar archivo de sedes");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos TXT", "*.txt"));

        return fileChooser.showOpenDialog(fileChooserStage);
    }

    @FXML
    protected void onLoadButtonClick() {
        if (!tablaSedes.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Cargar sedes");
            alert.setHeaderText("Ya hay información de sedes guardada");
            alert.setContentText("¿Desea mantenerla o eliminarla?");

            ButtonType buttonKeep = new ButtonType("Mantener");
            ButtonType buttonDelete = new ButtonType("Eliminar");
            ButtonType buttonCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonKeep, buttonDelete, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == buttonKeep) {
                    File file = fileChooser();

                    if (file != null) {
                        BD.getInstance().cargarSedes(file.toString());
                        cargar();
                    }
                } else if (result.get() == buttonDelete) {
                    File file = fileChooser();

                    if (file != null) {
                        BD.getInstance().eliminarTabla("Asignacion");
                        BD.getInstance().eliminarTabla("Realizacion");
                        BD.getInstance().eliminarTabla("Aula");
                        BD.getInstance().eliminarTabla("Sede");
                        BD.getInstance().cargarSedes(file.toString());
                        cargar();
                    }
                }
            }
        } else {
            File file = fileChooser();

            if (file != null) {
                BD.getInstance().cargarSedes(file.toString());
                cargar();
            }
        }
    }

    @FXML
    protected void onAddButtonClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Añadir sede");
        dialog.setHeaderText("Introduzca el nombre de la sede");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> BD.getInstance().anadirSede(new Sede(nombre)));
        if (result.isPresent()) cargar();
    }

    @FXML
    protected void onEditButtonClick() {
        Sede antigua = tablaSedes.getSelectionModel().getSelectedItem();
        String titulo = "Editar sede";

        if (antigua != null) {
            TextInputDialog dialog = new TextInputDialog(antigua.getNombre());
            dialog.setTitle(titulo);
            dialog.setHeaderText("Modifique el nombre de la sede");
            dialog.setContentText("Nombre:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(nuevo -> BD.getInstance().editarSede(nuevo, antigua.getNombre()));

            if (result.isPresent()) cargar();
        } else {
            sinSeleccion(titulo);
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        Sede sede = tablaSedes.getSelectionModel().getSelectedItem();
        String titulo = "Eliminar sede";

        if (sede != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle(titulo);
            alert.setHeaderText("Se eliminará la sede " + sede.getNombre());
            alert.setContentText("¿Está seguro?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {
                    BD.getInstance().eliminarSede(sede);
                    cargar();
                }
            }
        } else {
            sinSeleccion(titulo);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargar();
        BD.getInstance().setControladorSede(this);
    }
}
