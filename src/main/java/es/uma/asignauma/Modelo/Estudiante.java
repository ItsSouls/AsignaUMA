package es.uma.asignauma.Modelo;

import java.util.List;

public class Estudiante extends Persona {
    private Instituto centro;
    private List<Materia> materias;

    public Estudiante(Instituto centro, String nombre, String primerApellido, String segundoApellido, String dni) {
        super(nombre, primerApellido, segundoApellido, dni);
        this.setCentro(centro);
        this.setMaterias(null);
    }

    public Estudiante(Instituto centro, String nombre, String primerApellido, String segundoApellido, String dni, List<Materia> materias) {
        super(nombre, primerApellido, segundoApellido, dni);
        this.setCentro(centro);
        this.setMaterias(materias);
    }

    public Instituto getCentro() {
        return centro;
    }

    public List<Materia> getMaterias() {
        if (materias == null) {
            materias = Materia.getMateriasEstudiante(this);
        }

        return materias;
    }

    public String materiasString() {
        StringBuilder lista = new StringBuilder();
        List<Materia> mat = getMaterias();

        for (int i = 0; i < mat.size(); i++) {
            if (i < mat.size() - 1) {
                lista.append(mat.get(i).getNombre()).append(",");
            } else {
                lista.append(mat.get(i).getNombre());
            }
        }

        return lista.toString();
    }

    public void setCentro(Instituto centro) {
        this.centro = centro;
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias;
    }
}
