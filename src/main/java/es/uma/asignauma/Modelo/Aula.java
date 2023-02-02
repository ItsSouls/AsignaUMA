package es.uma.asignauma.Modelo;

public class Aula {
    private String nombre;
    private Sede sede;
    private int aforoTotal;
    private int aforoDisponible;
    private String horario;
    private int huecosL;

    public Aula(String nombre) {
        this.nombre = nombre;
    }

    public Aula(String nombre, Sede sede, int aforoTotal, String horario,int aforoDisponible) {
        setNombre(nombre);
        setSede(sede);
        setAforoTotal(aforoTotal);
        setAforoDisponible(aforoDisponible);
        setHorario(horario);
    }

    public Aula(String nombre, Sede sede, int aforoTotal, String horario) {
        this.nombre = nombre;
        this.sede = sede;
        this.aforoTotal = aforoTotal;
        this.horario = horario;
    }

    public Aula(String nombre, Sede sede, int aforo, String horario, int aforoD, int huecosL) {
        setNombre(nombre);
        setSede(sede);
        setAforoTotal(aforo);
        setAforoDisponible(aforoD);
        setHorario(horario);
        this.huecosL=huecosL;

    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public int getAforoTotal() {
        return aforoTotal;
    }

    public void setAforoTotal(int aforoTotal) {
        this.aforoTotal = aforoTotal;
    }

    public int getAforoDisponible() {
        return aforoDisponible;
    }

    public void setAforoDisponible(int aforoDisponible) {
        this.aforoDisponible = aforoDisponible;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aula)) return false;

        Aula aula = (Aula) o;

        return getNombre().equals(aula.getNombre());
    }

    @Override
    public int hashCode() {
        return getNombre().hashCode();
    }
}
