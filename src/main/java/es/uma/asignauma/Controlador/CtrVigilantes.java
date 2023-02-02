package es.uma.asignauma.Controlador;

import es.uma.asignauma.Modelo.Aula;
import es.uma.asignauma.Modelo.BD;
import es.uma.asignauma.Modelo.Usuario;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class CtrVigilantes implements Initializable {
    @FXML
    private TableView<Usuario> tablaVigilantes;
    @FXML
    private TableColumn<Usuario, String> columnaVigilante;
    @FXML
    private TableColumn<Usuario, ComboBox<Aula>> columnaAula;
    @FXML
    private TableColumn<Usuario, ComboBox<String>> columnaHorario;
    @FXML
    private TextField buscarVigilante;
    private ObservableList<Usuario> vigilantes;

    public void cargar() {
        vigilantes = FXCollections.observableArrayList(BD.getInstance().leerUsuarios(2));
        columnaVigilante.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getNombre()));
        columnaAula.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getAulaBox()));
        columnaHorario.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getHoraBox()));

        tablaVigilantes.setItems(vigilantes);
    }

    @FXML
    protected void onSearchButtonClick() {
        FilteredList<Usuario> usuarioFiltrado = new FilteredList<>(vigilantes, b -> true);
        String palabraBuscada = buscarVigilante.getText().toLowerCase();

        usuarioFiltrado.setPredicate(usuario -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            return usuario.getNombre().toLowerCase().contains(palabraBuscada);
        });

        SortedList<Usuario> sedesOrdenadas = new SortedList<>(usuarioFiltrado);

        sedesOrdenadas.comparatorProperty().bind(tablaVigilantes.comparatorProperty());

        tablaVigilantes.setItems(sedesOrdenadas);
    }

    @FXML
    protected void onEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSearchButtonClick();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargar();
        BD.getInstance().setControladorVigilantes(this);
    }
}
