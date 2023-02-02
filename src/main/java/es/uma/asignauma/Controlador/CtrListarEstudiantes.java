package es.uma.asignauma.Controlador;


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

public class CtrListarEstudiantes implements Initializable {
    @FXML
    private TableView<EstudianteLista> tablaEstudiantes;
    @FXML
    private TableColumn<EstudianteLista, String> columnaSede;
    @FXML
    private TableColumn<EstudianteLista, String> columnaNombre;
    @FXML
    private TableColumn<EstudianteLista, String> columnaApellidos;
    @FXML
    private TableColumn<EstudianteLista, String> columnaAula;
    @FXML
    private TableColumn<EstudianteLista, String> columnaHorario;
    @FXML
    private TableColumn<EstudianteLista, String> columnaMateria;
    @FXML
    private TextField buscarEstudiante;
    private ObservableList<EstudianteLista> estudiantes;

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
        estudiantes = FXCollections.observableArrayList(BD.getInstance().leerEstudiantesLista());
        columnaNombre.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getNombre()));
        columnaApellidos.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getApellidos()));
        columnaSede.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getSede()));
        columnaAula.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getAula()));
        columnaHorario.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getHorario()));
        columnaMateria.setCellValueFactory(n -> new ReadOnlyObjectWrapper<>(n.getValue().getMateria()));
        tablaEstudiantes.setItems(estudiantes);
    }


    @FXML
    protected void onSearchButtonClick() {
        FilteredList<EstudianteLista> estudianteFiltrado = new FilteredList<>(estudiantes, b -> true);
        String palabraBuscada = buscarEstudiante.getText().toLowerCase();

        estudianteFiltrado.setPredicate(estudiante -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            if (estudiante.getInstituto().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getNombre().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getApellidos().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getAula().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else if (estudiante.getMateria().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else return estudiante.getSede().toLowerCase().contains(palabraBuscada);
        });

        SortedList<EstudianteLista> estudiantesOrdenados = new SortedList<>(estudianteFiltrado);

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
    protected void cerrar(){
        Stage stage = (Stage) tablaEstudiantes.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        escribirInfo();
    }
}

