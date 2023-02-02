package es.uma.asignauma.Controlador;

import es.uma.asignauma.Main;
import es.uma.asignauma.Modelo.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CtrEstudiantes implements Initializable {
    @FXML
    private TableView<Estudiante> tablaEstudiantes;
    @FXML
    private TableColumn<Estudiante, String> columnaCentro;
    @FXML
    private TableColumn<Estudiante, String> columnaNombre;
    @FXML
    private TableColumn<Estudiante, String> columnaPrimerApellido;
    @FXML
    private TableColumn<Estudiante, String> columnaSegundoApellido;
    @FXML
    private TableColumn<Estudiante, String> columnaDni;
    @FXML
    private TableColumn<Estudiante, String> columnaMaterias;
    @FXML
    private TextField buscarEstudiante;
    @FXML
    private ListView<String> log;
    private ObservableList<Estudiante> estudiantes;

    private void procesarChunk(List<String> chunk, BufferedWriter bw, BufferedWriter log) throws IOException {
        for (String line : chunk) {
            if (line.chars().filter(ch -> ch == ';').count() != 5) {
                log.write(line + "\n");
            } else {
                bw.write(line + "\n");
            }
        }
    }

    private void comprobarEstudiantes(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            BufferedWriter bw = new BufferedWriter(new FileWriter("estudiantes.csv"));
            BufferedWriter bwlog = new BufferedWriter(new FileWriter("log.csv"));

            String line = br.readLine();

            if (line != null) {
                List<String> chunk = new ArrayList<>(1000);
                while ((line = br.readLine()) != null) {
                    chunk.add(line);
                    if (chunk.size() == 1000) {
                        procesarChunk(chunk, bw, bwlog);
                        chunk.clear();
                    }
                }

                if (!chunk.isEmpty()) {
                    procesarChunk(chunk, bw, bwlog);
                }
            }

            br.close();
            bw.close();
            bwlog.close();
        } catch (IOException e) {
            throw new BDException("No se ha podido abrir el archivo: " + e);
        }
    }

    private void escribirInfo() {
        estudiantes = FXCollections.observableArrayList(BD.getInstance().leerEstudiantes());

        columnaCentro.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCentro().toString()));
        columnaNombre.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getNombre()));
        columnaPrimerApellido.setCellValueFactory(pa -> new ReadOnlyObjectWrapper<>(pa.getValue().getPrimerApellido()));
        columnaSegundoApellido.setCellValueFactory(sa -> new ReadOnlyObjectWrapper<>(sa.getValue().getSegundoApellido()));
        columnaDni.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getDni()));
        columnaMaterias.setCellValueFactory(m -> new ReadOnlyObjectWrapper<>(m.getValue().materiasString()));

        tablaEstudiantes.setItems(estudiantes);
    }

    private void cargar(File file) {
        comprobarEstudiantes(file);
        BD.getInstance().cargarEstudiantes("estudiantes.csv");
        BD.getInstance().leerMatriculaciones("estudiantes.csv");
        BD.getInstance().cargarRegistro("log.csv");
        escribirInfo();
        log.getItems().clear();
        log.getItems().addAll(BD.getInstance().leerRegistro());
    }

    private File fileChooser() {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Seleccionar archivo de estudiantes");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        return fileChooser.showOpenDialog(fileChooserStage);
    }

    @FXML
    protected void onSearchButtonClick() {
        FilteredList<Estudiante> estudianteFiltrado = new FilteredList<>(estudiantes, b -> true);
        String palabraBuscada = buscarEstudiante.getText().toLowerCase();

        estudianteFiltrado.setPredicate(estudiante -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            if (estudiante.getCentro().toString().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getNombre().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getPrimerApellido().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getSegundoApellido().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getDni().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else return estudiante.materiasString().toLowerCase().contains(palabraBuscada);
        });

        SortedList<Estudiante> estudiantesOrdenados = new SortedList<>(estudianteFiltrado);

        estudiantesOrdenados.comparatorProperty().bind(tablaEstudiantes.comparatorProperty());

        tablaEstudiantes.setItems(estudiantesOrdenados);
    }

    @FXML
    protected void onEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSearchButtonClick();
        }
    }

    @FXML
    protected void onLoadButtonClick() {
        if (!tablaEstudiantes.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Cargar estudiantes");
            alert.setHeaderText("Ya hay información de estudiantes guardada");
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
                        cargar(file);
                    }
                } else if (result.get() == buttonDelete) {
                    File file = fileChooser();

                    if (file != null) {
                        BD.getInstance().eliminarTabla("Matriculacion");
                        BD.getInstance().eliminarTabla("Estudiante");
                        BD.getInstance().cargarMaterias();
                        cargar(file);
                    }
                }
            }
        } else {
            File file = fileChooser();

            if (file != null) {
                BD.getInstance().cargarMaterias();
                cargar(file);
            }
        }
    }

    @FXML
    protected void editOnAction() {
        if (log.getSelectionModel().getSelectedItems().size() == 1) {
            Dialog<Estudiante> dialog = new Dialog<>();
            dialog.setTitle("Editar log");

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20, 10, 10, 10));

            TextField centro = new TextField();
            centro.setPromptText("Centro");
            TextField nombre = new TextField();
            nombre.setPromptText("Nombre");
            TextField primerApellido = new TextField();
            primerApellido.setPromptText("Primer apellido");
            TextField segundoApellido = new TextField();
            segundoApellido.setPromptText("Segundo apellido");
            TextField dni = new TextField();
            dni.setPromptText("D.N.I.");
            TextField materias = new TextField();
            materias.setPromptText("Materias");

            gridPane.add(new Label("Centro:"), 0, 0);
            gridPane.add(centro, 0, 1);
            gridPane.add(new Label("Nombre:"), 0, 2);
            gridPane.add(nombre, 0, 3);
            gridPane.add(new Label("Primer apellido:"), 0, 4);
            gridPane.add(primerApellido, 0, 5);
            gridPane.add(new Label("Segundo apellido:"), 0, 6);
            gridPane.add(segundoApellido, 0, 7);
            gridPane.add(new Label("D.N.I.:"), 0, 8);
            gridPane.add(dni, 0, 9);
            gridPane.add(new Label("Materias:"), 0, 10);
            gridPane.add(materias, 0, 11);

            dialog.getDialogPane().setContent(gridPane);

            Platform.runLater(centro::requestFocus);

            List<Materia> listaMaterias = new ArrayList<>();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new Estudiante(new Instituto(centro.getText()), nombre.getText(), primerApellido.getText(), segundoApellido.getText(), dni.getText(), listaMaterias);
                }
                return null;
            });

            Optional<Estudiante> result = dialog.showAndWait();

            for (String nombreMateria : materias.getText().split(",")) {
                listaMaterias.add(new Materia(nombreMateria));
            }

            result.ifPresent(estudiante -> {
                BD.getInstance().anadirEstudiante(estudiante);
                escribirInfo();
                BD.getInstance().eliminarRegistro(log.getSelectionModel().getSelectedItem());
                log.getItems().remove(log.getSelectionModel().getSelectedIndex());
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Editar log");
            alert.setHeaderText(null);
            alert.setContentText("No ha seleccionado ningún elemento del log");

            alert.showAndWait();
        }
    }

    public void listarEstudiantes() throws IOException {
        Stage secondaryStage=new Stage();
        FXMLLoader loader= new FXMLLoader(Main.class.getResource("Vista/VistaListarEstudiantes.fxml"));
        secondaryStage.setTitle("Listado de estudiantes");
        secondaryStage.setScene(new Scene(loader.load(), 1200, 600));
        secondaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BD.getInstance().cargarMaterias();
        escribirInfo();
        log.getItems().addAll(BD.getInstance().leerRegistro());
        BD.getInstance().cargarRespSedes();
    }
}
