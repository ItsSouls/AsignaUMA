package es.uma.asignauma.Modelo;

public class Funcion {
    int rol;
    String aula;
    String hora;

    public Funcion(int rol, String aula, String hora) {
        this.rol = rol;
        this.aula = aula;
        this.hora = hora;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
