package es.uma.asignauma.Controlador;

import es.uma.asignauma.Modelo.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CtrAulas implements Initializable {
    @FXML
    public TableView<Aula> tablaAulas;
    @FXML
    public TableColumn<Aula, String> columnaAula;
    @FXML
    public TableColumn<Aula, String> columnaSede;
    @FXML
    public TableColumn<Aula, String> columnaAforo;
    @FXML
    public TableColumn<Aula, String> columnaHorario;
    @FXML
    private TextField buscarAula;
    private ObservableList<Aula> aulas;

    private void cargar() {
        aulas = FXCollections.observableArrayList(BD.getInstance().leerAulas());
        columnaSede.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getSede().getNombre()));
        columnaAula.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getNombre()));
        columnaAforo.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(String.valueOf(s.getValue().getAforoTotal())));
        columnaHorario.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getHorario()));

        tablaAulas.setItems(aulas);
    }

    private void sinSeleccion(String titulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText("No ha seleccionado ningún aula");

        alert.showAndWait();
    }

    @FXML
    protected void onSearchButtonClick() {
        FilteredList<Aula> aulaFiltrada = new FilteredList<>(aulas, b -> true);
        String palabraBuscada = buscarAula.getText().toLowerCase();

        aulaFiltrada.setPredicate(estudiante -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            return estudiante.getNombre().toLowerCase().contains(palabraBuscada);
        });

        SortedList<Aula> aulasOrdenadas = new SortedList<>(aulaFiltrada);

        aulasOrdenadas.comparatorProperty().bind(tablaAulas.comparatorProperty());

        tablaAulas.setItems(aulasOrdenadas);
    }

    @FXML
    protected void onEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSearchButtonClick();
        }
    }

    @FXML
    protected void onAddButtonClick() {
        Dialog<Aula> dialog = new Dialog<>();
        dialog.setTitle("Añadir aula");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre del aula");
        TextField sede = new TextField();
        sede.setPromptText("Nombre de la sede");
        TextField aforo = new TextField();
        aforo.setPromptText("Aforo del aula");
        TextField horario = new TextField();
        horario.setPromptText("Horario de disponibilidad");

        gridPane.add(new Label("Nombre:"), 0, 0);
        gridPane.add(nombre, 0, 1);
        gridPane.add(new Label("Sede:"), 0, 2);
        gridPane.add(sede, 0, 3);
        gridPane.add(new Label("Aforo:"), 0, 4);
        gridPane.add(aforo, 0, 5);
        gridPane.add(new Label("Horario:"), 0, 6);
        gridPane.add(horario, 0, 7);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(nombre::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Aula(nombre.getText(), new Sede(sede.getText()), Integer.parseInt(aforo.getText()), horario.getText());
            }
            return null;
        });

        Optional<Aula> result = dialog.showAndWait();

        result.ifPresent(aula -> BD.getInstance().anadirAula(aula));
        if (result.isPresent()) {
            cargar();
            BD.getInstance().getControladorSede().cargar();
            BD.getInstance().getControladorExamenes().cargar();
            BD.getInstance().getControladorInstitutos().cargar();
        }
    }

    @FXML
    protected void onEditButtonClick() {
        Aula antigua = tablaAulas.getSelectionModel().getSelectedItem();
        String titulo = "Editar aula";

        if (antigua != null) {
            Dialog<Aula> dialog = new Dialog<>();
            dialog.setTitle("Editar aula");

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20, 150, 10, 10));

            TextField nombre = new TextField(antigua.getNombre());
            nombre.setPromptText("Nombre del aula");
            TextField sede = new TextField(antigua.getSede().getNombre());
            sede.setPromptText("Nombre de la sede");
            TextField aforo = new TextField(String.valueOf(antigua.getAforoTotal()));
            aforo.setPromptText("Aforo del aula");
            TextField horario = new TextField(antigua.getHorario());
            horario.setPromptText("Horario de disponibilidad");

            gridPane.add(new Label("Nombre:"), 0, 0);
            gridPane.add(nombre, 0, 1);
            gridPane.add(new Label("Sede:"), 0, 2);
            gridPane.add(sede, 0, 3);
            gridPane.add(new Label("Aforo:"), 0, 4);
            gridPane.add(aforo, 0, 5);
            gridPane.add(new Label("Horario:"), 0, 6);
            gridPane.add(horario, 0, 7);

            dialog.getDialogPane().setContent(gridPane);

            Platform.runLater(nombre::requestFocus);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new Aula(nombre.getText(), new Sede(sede.getText()), Integer.parseInt(aforo.getText()), horario.getText());
                }
                return null;
            });

            Optional<Aula> result = dialog.showAndWait();

            result.ifPresent(aula -> {
                BD.getInstance().editarAula(aula, antigua.getNombre(), horario.getText());
                cargar();
                BD.getInstance().getControladorSede().cargar();
            });
        } else {
            sinSeleccion(titulo);
        }

    }

    @FXML
    protected void onDeleteButtonClick() {
        Aula aula = tablaAulas.getSelectionModel().getSelectedItem();
        String titulo = "Eliminar aula";

        if (aula != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle(titulo);
            alert.setHeaderText("Se eliminará el aula " + aula.getNombre());
            alert.setContentText("¿Está seguro?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {
                    BD.getInstance().eliminarAula(aula);
                    cargar();
                    BD.getInstance().getControladorSede().cargar();
                }
            }
        } else {
            sinSeleccion(titulo);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargar();
    }
}
