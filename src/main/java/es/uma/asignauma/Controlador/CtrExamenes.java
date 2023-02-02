package es.uma.asignauma.Controlador;

import es.uma.asignauma.Modelo.Aula;
import es.uma.asignauma.Modelo.BD;
import es.uma.asignauma.Modelo.Materia;
import es.uma.asignauma.Modelo.Sede;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class CtrExamenes implements Initializable {
    @FXML
    private TextField buscarExamen;
    @FXML
    public ComboBox<Sede> sedeComboBox;
    @FXML
    private TableView<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>> tablaExamenes;
    @FXML
    private TableColumn<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>, Object> columnaMateria;
    @FXML
    private TableColumn<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>, Object> columnaHorario;
    @FXML
    private TableColumn<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>, Object> columnaAula;
    private ObservableList<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>> datos;
    protected void cargar() {
        datos = FXCollections.observableArrayList();
        ObservableList<Materia> materias = FXCollections.observableArrayList(BD.getInstance().leerMaterias());

        columnaMateria.setCellValueFactory(m -> new ReadOnlyObjectWrapper<>(m.getValue().getKey().getNombre()));
        columnaHorario.setCellValueFactory(h -> new ReadOnlyObjectWrapper<>(h.getValue().getValue().getKey()));
        columnaAula.setCellValueFactory(a -> new ReadOnlyObjectWrapper<>(a.getValue().getValue().getValue()));

        for (Materia materia : materias) {
            final boolean[] x = {true};
            ObservableList<Aula> aulas = FXCollections.observableArrayList(BD.getInstance().leerAulasPorSede(sedeComboBox.getValue(), materia));


            //aulas.removeIf(aula -> aula.getAforoDisponible() == 0);

            CheckComboBox<Aula> aulaCheckComboBox = new CheckComboBox<>(aulas);

            aulaCheckComboBox.setPrefWidth(200);



            List<Aula> seleccionAulas = BD.getInstance().leerRealizaciones(materia);

            if (!seleccionAulas.isEmpty()) {
                for (Aula aula : seleccionAulas) {
                    for (int i = 0; i < aulaCheckComboBox.getItems().size(); i++) {
                        if (aula.equals(aulaCheckComboBox.getCheckModel().getItem(i))) {
                            aulaCheckComboBox.getCheckModel().check(i);
                        }
                    }
                }
            }
            aulaCheckComboBox.getItems().removeIf(aula -> aula.getAforoDisponible() == 0 && !aulaCheckComboBox.getCheckModel().isChecked(aula));
            aulaCheckComboBox.setDisable((aulas.isEmpty() && aulaCheckComboBox.getCheckModel().getCheckedItems().size()==0) || BD.getInstance().contarMatriculasPorMateriaYSede(materia, sedeComboBox.getValue())==0);

            ObservableList<Aula> aulasAsignadas = aulaCheckComboBox.getCheckModel().getCheckedItems();

            aulasAsignadas.addListener((ListChangeListener.Change<? extends Aula> a) -> {
                boolean todosAsignados=false;
                if(x[0]){
                    int aforoDisponibleAulasAsignadas = aulasAsignadas.stream().mapToInt(Aula::getAforoDisponible).sum();
                    int matriculados = BD.getInstance().contarMatriculasPorMateriaYSede(materia, sedeComboBox.getValue());
                    int matriculadosPorAsignar = matriculados - aforoDisponibleAulasAsignadas;
                    int numAsignadosSede = BD.getInstance().obtenerNumAsignados(materia,sedeComboBox.getValue());

                    while (a.next()) {
                        boolean asignar=true;
                        if (a.wasAdded()) {
                            if(matriculados-numAsignadosSede<=0){
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                                alert.setTitle("Aforos");
                                alert.setHeaderText("Ya están asignados todos los alumnos");
                                alert.setContentText("Tienes que desmarcar primero el aula ya seleccionada si quieres cambiar de aula");

                                alert.showAndWait();
                                asignar=false;
                                x[0] =false;
                                break;
                            }
                            Aula aula = a.getAddedSubList().get(0);
                            if (matriculadosPorAsignar >= 0) {
                                aula.setAforoDisponible(0);
                                BD.getInstance().actualizarAforoD(aula,0);
                                BD.getInstance().actualizarNumAsignados(materia,aula,aula.getAforoTotal());
                                BD.getInstance().actualizarHuecosL(aula,0);
                            } else {
                                aula.setAforoDisponible(Math.abs(matriculadosPorAsignar)+numAsignadosSede);
                                BD.getInstance().actualizarAforoD(aula,Math.abs(matriculadosPorAsignar)+numAsignadosSede);
                                BD.getInstance().actualizarNumAsignados(materia,aula,matriculados-numAsignadosSede);
                                BD.getInstance().actualizarHuecosL(aula,Math.abs(matriculadosPorAsignar)+numAsignadosSede);
                            }



                            if (matriculadosPorAsignar > 0) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                                alert.setTitle("Aforos");
                                alert.setHeaderText("Es necesario añadir más aulas");
                                alert.setContentText("Matriculados por asignar: " + matriculadosPorAsignar);
                                //BD.getInstance().actualizarNumAsignados(materia,aula,matriculados-matriculadosPorAsignar);

                                alert.showAndWait();


                            } else if (matriculadosPorAsignar < 0) {
                                //Aula aula;

                                if (a.wasAdded()) {
                                    aula = a.getAddedSubList().get(0);
                                } else {
                                    aula = a.getRemoved().get(0);
                                }
                                //BD.getInstance().actualizarNumAsignados(materia,aula,matriculados);

                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                                alert.setTitle("Aforos");
                                int num=Math.abs(matriculadosPorAsignar)+numAsignadosSede;
                                alert.setHeaderText("Quedan " + num + " plazas por asignar en " + aula);
                                alert.setContentText("¿Desea completar aforo con otra materia?");
                                ButtonType buttonConfirm = new ButtonType("SÍ");
                                ButtonType buttonCancel = new ButtonType("NO", ButtonBar.ButtonData.CANCEL_CLOSE);
                                alert.getButtonTypes().setAll(buttonConfirm,buttonCancel);

                                Optional<ButtonType> result = alert.showAndWait();

                                if (result.isPresent()) {
                                    if (result.get() == buttonCancel) {
                                        BD.getInstance().actualizarAforoD(aula,0);
                                    }
                                }
                            }
                            else{
                                BD.getInstance().actualizarNumAsignados(materia,aula,matriculados);
                            }
                        }



                        else if (a.wasRemoved()) {
                            Aula aula = a.getRemoved().get(0);
                            int matriculadosAula=BD.getInstance().obtenerNumAsignados(materia,aula);
                            int huecosLibres=BD.getInstance().obtenerHuecosLibres(aula, materia.getHorario());
                            if(huecosLibres!=aula.getAforoDisponible()){
                                BD.getInstance().actualizarAforoD(aula,huecosLibres+matriculadosAula);
                                BD.getInstance().actualizarHuecosL(aula,huecosLibres+matriculadosAula);
                            }
                            else{
                                BD.getInstance().actualizarAforoD(aula,aula.getAforoDisponible()+matriculadosAula);
                                BD.getInstance().actualizarHuecosL(aula,aula.getAforoDisponible()+matriculadosAula);
                            }
                            BD.getInstance().actualizarNumAsignados(materia,aula,0);
                        }

                        if(asignar){
                            BD.getInstance().actualizarExamen(materia, aulasAsignadas);

                        }
                        x[0] =false;

                    }
                    cargar();
                }

            });

            Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>> filaDatos = Map.entry(materia, Map.entry(materia.getHorario(), aulaCheckComboBox));

            datos.add(filaDatos);
        }

        tablaExamenes.setItems(datos);
        onSearchButtonClick();
    }

    @FXML
    protected void onSearchButtonClick() {
        FilteredList<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>> materiaFiltrada = new FilteredList<>(datos, b -> true);
        String palabraBuscada = buscarExamen.getText().toLowerCase();

        materiaFiltrada.setPredicate(materia -> {

            if (palabraBuscada.isEmpty() || palabraBuscada.isBlank()) {
                return true;
            }

            if (materia.getKey().getNombre().toLowerCase().contains(palabraBuscada)) {
                return true;
            } else return materia.getValue().getKey().toLowerCase().contains(palabraBuscada);
        });

        SortedList<Map.Entry<Materia, Map.Entry<String, CheckComboBox<Aula>>>> materiasOrdenadas = new SortedList<>(materiaFiltrada);

        materiasOrdenadas.comparatorProperty().bind(tablaExamenes.comparatorProperty());

        tablaExamenes.setItems(materiasOrdenadas);
    }

    @FXML
    protected void onEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSearchButtonClick();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<Sede> sedes = FXCollections.observableArrayList(BD.getInstance().leerTodasSedes());
        sedeComboBox.setItems(sedes);

        sedeComboBox.setValue(sedes.get(0));

        sedeComboBox.setOnAction(event -> cargar());

        cargar();
        BD.getInstance().setControladorExamenes(this);
    }
}
