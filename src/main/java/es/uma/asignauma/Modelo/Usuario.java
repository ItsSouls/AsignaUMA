package es.uma.asignauma.Modelo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;

public class Usuario extends Persona {
    private Funcion funcion;
    private ComboBox aulaBox;
    private ComboBox horaBox;
    private String aulaAnterior = "";
    private String horaAnterior = "";
    public String horaComboInicial;
    public String aulaComboInicial;

    public Usuario(String nombre, String primerApellido, String segundoApellido, String dni) {
        super(nombre, primerApellido, segundoApellido, dni);
        funcion = null;
        aulaBox = null;
        horaBox = null;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public void setFuncion(Funcion f) {
        this.funcion = f;
    }

    public ComboBox getAulaBox() {
        return aulaBox;
    }

    public ComboBox getHoraBox() {
        return horaBox;
    }

    public void setAulaAnterior(String aulaAnterior) {
        this.aulaAnterior = aulaAnterior;
    }

    public void setHoraAnterior(String horaAnterior) {
        this.horaAnterior = horaAnterior;
    }

    public String getAulaAnterior() {
        return aulaAnterior;
    }

    public String getHoraAnterior() {
        return horaAnterior;
    }

    public void setAulaBox(ComboBox aulaBox) {
        this.aulaBox = aulaBox;
        Usuario u = this;
        if (this.aulaBox.getValue() == null) this.aulaBox.setValue("");
        EventHandler evento = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                BD.getInstance().cambio = true;
                if (aulaBox.getValue() == null) aulaBox.setValue("");
                if (aulaBox.getValue().toString().equals("")) {
                    if (horaBox.getValue().toString().equals("")) {
                        BD.getInstance().actualizarUsuario2(u, new Funcion(0, aulaBox.getValue().toString(), horaBox.getValue().toString()));
                    }
                } else {
                    if (!horaBox.getValue().toString().equals("")) {
                        BD.getInstance().actualizarUsuario(u, new Funcion(BD.getInstance().getPestana(), aulaBox.getValue().toString(), horaBox.getValue().toString()));
                    }
                }
            }
        };
        this.aulaBox.setOnAction(evento);
    }


    public void setHoraBox(ComboBox horaBox) {
        this.horaBox = horaBox;
        Usuario u = this;
        if (this.horaBox.getValue() == null) this.horaBox.setValue("");
        EventHandler evento = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                BD.getInstance().cambio = true;
                List<String> aulasPrueba = new ArrayList<>();
                if (horaBox.getValue() == null) horaBox.setValue("");
                aulasPrueba.addAll(BD.getInstance().leerAulas(u.horaBox.getValue().toString(), BD.getInstance().pestana, u.getNombre()));
                ObservableList<String> aulasPr = FXCollections.observableArrayList(aulasPrueba);
                aulaBox.setItems(aulasPr);

                if (horaBox.getValue().toString().equals("")) {
                    aulaBox.setDisable(true);
                    BD.getInstance().actualizarUsuario2(u, new Funcion(0, aulaBox.getValue().toString(), getHoraAnterior()));
                } else {
                    if (horaBox.getValue().toString().equals(horaComboInicial)) aulaBox.setValue(aulaComboInicial);
                    else aulaBox.setValue("");
                    aulaBox.setDisable(false);
                    if (!aulaBox.getValue().toString().equals("")) {
                        //BD.getInstance().actualizarUsuario(u, new Funcion(BD.getInstance().getPestana(), aulaBox.getValue().toString(), horaBox.getValue().toString()));
                    }
                }
            }
        };
        this.horaBox.setOnAction(evento);
    }
}


