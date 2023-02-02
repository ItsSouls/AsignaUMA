package es.uma.asignauma.Modelo;

import java.util.List;

public class Materia {
    private String nombre;

    private String horario;

    public Materia(String nombre) {
        this.setNombre(nombre);
        this.setHorario(null);
    }

    public Materia(String nombre, String horario) {
        this.setNombre(nombre);
        this.setHorario(horario);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public static List<Materia> getMateriasEstudiante(Estudiante e) {
        return BD.getInstance().materiasEstudiante(e);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
