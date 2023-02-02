package es.uma.asignauma.Modelo;

public class EstudianteLista {
    private String nombre;
    private String apellidos;
    private String sede;
    private String materia;
    private String aula;
    private String horario;
    private String instituto;

    public EstudianteLista(String nombre, String apellidos, String sede, String materia, String aula, String horario, String instituto) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.sede = sede;
        this.materia = materia;
        this.aula = aula;
        this.horario = horario;
        this.instituto = instituto;
    }

    public EstudianteLista() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getInstituto() {
        return instituto;
    }

    public void setInstituto(String instituto) {
        this.instituto = instituto;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
