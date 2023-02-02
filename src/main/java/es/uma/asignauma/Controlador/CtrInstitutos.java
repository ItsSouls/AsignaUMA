package es.uma.asignauma.Controlador;

import es.uma.asignauma.Modelo.BD;
import es.uma.asignauma.Modelo.Instituto;
import es.uma.asignauma.Modelo.Sede;
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

import java.util.Map;

import java.net.URL;
import java.util.ResourceBundle;

public class CtrInstitutos implements Initializable {
    @FXML
    private TableView<Map.Entry<Instituto, ComboBox<Sede>>> tablaInstitutos;
    @FXML
    private TableColumn<Map.Entry<Instituto, ComboBox<Sede>>, Object> columnaInstituto;
    @FXML
    private TableColumn<Map.Entry<Instituto, ComboBox<Sede>>, Object> columnaSede;
    @FXML
    private TextField buscarInstituto;
    private ObservableList<Map.Entry<Instituto, ComboBox<Sede>>> datos;

    public void cargar() {
        datos = FXCollections.observableArrayList();
        ObservableList<Instituto> institutos = FXCollections.observableArrayList(BD.getInstance().leerInstitutos());
        ObservableList<Sede> sedes = FXCollections.observableArrayList();
        sedes.add(new Sede(""));
        sedes.addAll(BD.getInstance().leerTodasSedes());

        columnaInstituto.setCellValueFactory(i -> new ReadOnlyObjectWrapper<>(i.getValue().getKey()));
        columnaSede.setCellValueFactory(s -> new ReadOnlyObjectWrapper<>(s.getValue().getValue()));

        for (Instituto instituto : institutos) {
            int numAlumnos = BD.getInstance().numAlumnos(instituto);
            ObservableList<Sede> sedesPosibles = FXCollections.observableArrayList();
            sedesPosibles.add(new Sede(""));
            sedesPosibles.addAll(BD.getInstance().sedesPosibles(numAlumnos));
            ComboBox<Sede> sedeComboBox = new ComboBox<>(sedesPosibles);

            sedeComboBox.setPrefWidth(200);

            sedeComboBox.setDisable(sedesPosibles.size() == 1);

            sedeComboBox.setValue(BD.getInstance().leerAsignaciones(instituto));

            sedeComboBox.setOnAction(event -> {
                BD.getInstance().actualizarInstituto(instituto, sedeComboBox.getSelectionModel().getSelectedItem());
                cargar();
            });

            Map.Entry<Instituto, ComboBox<Sede>> filaDatos = Map.entry(instituto, sedeComboBox);

            datos.add(filaDatos);
        }

        tablaInstitutos.setItems(datos);
    }

    @FXML
    protected void onSearchButtonClick() {
        FilteredList<Map.Entry<Instituto, ComboBox<Sede>>> institutoFiltrado = new FilteredList<>(datos, b -> true);
        String palabraBuscada = buscarInstituto.getText().toLowerCase();

        institutoFiltrado.setPredicate(instituto -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            if (instituto.getKey().getNombre().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else return instituto.getValue().toString().toLowerCase().contains(palabraBuscada);
        });

        SortedList<Map.Entry<Instituto, ComboBox<Sede>>> institutosOrdenados = new SortedList<>(institutoFiltrado);

        institutosOrdenados.comparatorProperty().bind(tablaInstitutos.comparatorProperty());

        tablaInstitutos.setItems(institutosOrdenados);
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
        BD.getInstance().setControladorInstitutos(this);
    }
}
