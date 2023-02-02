package es.uma.asignauma.Modelo;

import javafx.scene.control.ComboBox;

public class Sede {
    private String nombre;
    private int aforo;
    private ResponsableSede rSedeAsignado;
    private ComboBox rSedeBox;

    public Sede(String nombre) {
        setNombre(nombre);
        setAforo(0);
        setRSedeAsignado(null);
        rSedeBox = null;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setAforo(int aforo) {
        this.aforo = aforo;
    }

    public int getAforo() {
        return this.aforo;
    }

    public ResponsableSede getRSedeAsignado() {
        return rSedeAsignado;
    }

    public void setRSedeAsignado(ResponsableSede rSedeAsignado) {
        this.rSedeAsignado = rSedeAsignado;
    }

    public ComboBox getRSedeBox() {
        return rSedeBox;
    }

    public void setRSedeBox(ComboBox rSedeBox) {
        this.rSedeBox = rSedeBox;
        this.rSedeBox.setOnAction((e) -> BD.getInstance().actualizarSede(this, new ResponsableSede(rSedeBox.getValue().toString(), null, null, null)));
    }

    @Override
    public String toString() {
        return nombre;
    }
}
