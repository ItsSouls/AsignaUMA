package es.uma.asignauma.Controlador;

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

public class CtrRAula implements Initializable {
    @FXML
    private TableView<Usuario> tablaRAulas;
    @FXML
    private TableColumn<Usuario, String> columnaRAulas;
    @FXML
    private TableColumn<Usuario, ComboBox> columnaAula;
    @FXML
    private TableColumn<Usuario, ComboBox> columnaHorario;
    @FXML
    private TextField buscarRAula;
    private ObservableList<Usuario> rAulas;

    public void cargar() {
        rAulas = FXCollections.observableArrayList(BD.getInstance().leerUsuarios(1));
        columnaRAulas.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getNombre()));
        columnaAula.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getAulaBox()));
        columnaHorario.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getHoraBox()));

        tablaRAulas.setItems(rAulas);
    }

    @FXML
    protected void onSearchButtonClick() {
        FilteredList<Usuario> usuarioFiltrado = new FilteredList<>(rAulas, b -> true);
        String palabraBuscada = buscarRAula.getText().toLowerCase();

        usuarioFiltrado.setPredicate(usuario -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            if (usuario.getNombre().toLowerCase().contains(palabraBuscada)) {
                return true;
            }

            return false;
        });

        SortedList<Usuario> sedesOrdenadas = new SortedList<>(usuarioFiltrado);

        sedesOrdenadas.comparatorProperty().bind(tablaRAulas.comparatorProperty());

        tablaRAulas.setItems(sedesOrdenadas);
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
        BD.getInstance().setControladorRAula(this);
    }
}
