package es.uma.asignauma.Modelo;

import es.uma.asignauma.Controlador.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BD {
    static final String DB_URL = "jdbc:mysql://database-pevau.cobadwnzalab.eu-central-1.rds.amazonaws.com";
    static final String DB_SCHEMA = "grupo08DB";

    static final String USER = "grupo08";
    static final String PASS = "rGTdv4dWyeBES7R8";

    int pestana;
    public boolean cambio = false;

    public int getPestana() {
        return pestana;
    }

    public void setPestana(int p) {
        pestana = p;
    }

    CtrSedes controladorSede = null;
    CtrVigilantes controladorVigilantes = null;
    CtrRAula controladorRAula = null;
    CtrExamenes controladorExamenes = null;
    CtrInstitutos controladorInstitutos = null;

    public CtrInstitutos getControladorInstitutos() {
        return controladorInstitutos;
    }

    public void setControladorInstitutos(CtrInstitutos controladorInstitutos) {
        this.controladorInstitutos = controladorInstitutos;
    }

    public CtrExamenes getControladorExamenes() {
        return controladorExamenes;
    }

    public void setControladorExamenes(CtrExamenes controladorExamenes) {
        this.controladorExamenes = controladorExamenes;
    }

    public CtrSedes getControladorSede() {
        return controladorSede;
    }

    public CtrVigilantes getControladorVigilantes() {
        return controladorVigilantes;
    }

    public CtrRAula getControladorRAula() {
        return controladorRAula;
    }

    public void setControladorSede(CtrSedes controlador) {
        this.controladorSede = controlador;
    }

    public void setControladorVigilantes(CtrVigilantes controlador) {
        this.controladorVigilantes = controlador;
    }

    public void setControladorRAula(CtrRAula controlador) {
        this.controladorRAula = controlador;
    }

    private static Connection connection;

    private static BD bd = null;

    private BD() {
        try {
            connection = DriverManager.getConnection(DB_URL + "/" + DB_SCHEMA + "?allowLoadLocalInfile=true", USER, PASS);
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BD getInstance() {
        if (bd == null) {
            bd = new BD();
        }

        return bd;
    }

    public void eliminarTabla(String tabla) {
        String estudiantesQuery = "DELETE FROM " + tabla;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(estudiantesQuery);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("Error al eliminar los estudiantes");
        }
    }

    public List<Estudiante> leerEstudiantes() {
        String sqlQueryEstudiante = "SELECT * FROM Estudiante";
        List<Estudiante> estudiantes = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQueryEstudiante);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    estudiantes.add(new Estudiante(new Instituto(rs.getString(1)), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return estudiantes;
    }

    public List<EstudianteLista> leerEstudiantesLista() {
        List<EstudianteLista> lista = new ArrayList<>();
        String query = "SELECT * FROM Matriculacion;";

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    EstudianteLista e = new EstudianteLista();
                    e.setMateria(rs.getString(2));

                    String query2 = "SELECT * FROM Estudiante WHERE DNI=?;";
                    PreparedStatement stmt2 = connection.prepareStatement(query2);
                    stmt2.setString(1, rs.getString(1));
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.isBeforeFirst()) {
                        while (rs2.next()) {
                            e.setNombre(rs2.getString(2));
                            e.setApellidos(rs2.getString(3) + " " + rs2.getString(4));
                            e.setInstituto(rs2.getString(1));
                        }
                    }

                    String query3 = "SELECT Sede FROM Asignacion WHERE Instituto=?;";
                    PreparedStatement stmt3 = connection.prepareStatement(query3);
                    stmt3.setString(1, e.getInstituto());
                    ResultSet rs3 = stmt3.executeQuery();
                    if (rs3.isBeforeFirst()) {
                        while (rs3.next()) {
                            e.setSede(rs3.getString(1));
                        }
                    }

                    String query4 = "SELECT Aula FROM Realizacion WHERE Materia=? AND Aula IS NOT NULL AND Aula IN (SELECT Nombre FROM Aula WHERE Sede=?);";
                    PreparedStatement stmt4 = connection.prepareStatement(query4);
                    stmt4.setString(1, e.getMateria());
                    stmt4.setString(2, e.getSede());
                    ResultSet rs4 = stmt4.executeQuery();
                    if (rs4.isBeforeFirst()) {
                        while (rs4.next()) {
                            e.setAula(rs4.getString(1));
                        }
                    }

                    String query5 = "SELECT Horario FROM Materia WHERE Nombre=?;";
                    PreparedStatement stmt5 = connection.prepareStatement(query5);
                    stmt5.setString(1, e.getMateria());
                    ResultSet rs5 = stmt5.executeQuery();
                    if (rs5.isBeforeFirst()) {
                        while (rs5.next()) {
                            e.setHorario(rs5.getString(1));
                        }
                    }

                    lista.add(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;

    }

    public List<ResponsableSede> leerRespSedesLibres() {
        String sqlQuery = "SELECT Usuario FROM Administracion WHERE Sede IS NULL ORDER BY Usuario";
        List<ResponsableSede> usuarios = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    usuarios.add(new ResponsableSede(rs.getString(1), null, null, null));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return usuarios;
    }

    public List<String> leerAulas(String hora, int rol, String usuario) {
        String sqlQuery = "SELECT DISTINCT Nombre from Aula WHERE Horario='" + hora + "' AND Nombre NOT IN (SELECT Aula FROM Usuario WHERE Rol=1);";
        if (rol == 2 || rol == 0)
            sqlQuery = "SELECT DISTINCT Nombre from Aula WHERE Horario='" + hora + "' AND Nombre NOT IN (SELECT Aula FROM Usuario WHERE Nombre='" + usuario + "' AND Horario='" + hora + "');";
        List<String> aulas = new ArrayList<>();
        try {
            if (hora == null) sqlQuery = "SELECT DISTINCT Nombre from Aula;";
            else if (hora.equals("")) sqlQuery = "SELECT DISTINCT Nombre from Aula;";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    aulas.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return aulas;
    }

    public List<String> leerHoras(String aula, String usuario) {
        String sqlQuery = "SELECT Horario from Aula WHERE Nombre='" + aula + "' ORDER BY Horario;";
        List<String> horas = new ArrayList<>();
        try {
            if (aula == null)
                sqlQuery = "SELECT DISTINCT Horario from Materia WHERE Horario NOT IN (SELECT Horario FROM Usuario WHERE Nombre='" + usuario + "' AND Horario IS NOT NULL) ORDER BY Horario;";
            else if (aula.equals(""))
                sqlQuery = "SELECT DISTINCT Horario from WHERE Horario NOT IN (SELECT Horario FROM Usuario WHERE Nombre='" + usuario + "' AND Horario IS NOT NULL) ORDER BY Horario;";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    horas.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return horas;
    }

    public List<Usuario> leerUsuarios(int Rol) {
        String sqlQuery = "SELECT * FROM Usuario WHERE Rol=" + Rol + " OR Rol=0 ORDER BY Nombre, Rol DESC";
        List<Usuario> usuarios = new ArrayList<>();
        Map<String, Integer> visitados = new HashMap<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    Usuario u = new Usuario(rs.getString(1), null, null, null);
                    u.setFuncion(new Funcion(rs.getInt(2), rs.getString(3), rs.getString(4)));

                    List<String> horasPrueba = new ArrayList<>();
                    horasPrueba.add("");
                    horasPrueba.addAll(leerHoras(null, u.getNombre()));
                    List<String> aulasPrueba = new ArrayList<>(leerAulas(u.getFuncion().hora, Rol, u.getNombre()));
                    ComboBox<String> comAula = new ComboBox<>();
                    ComboBox<String> comHora = new ComboBox<>();
                    ObservableList<String> aulasPr = FXCollections.observableArrayList(aulasPrueba);
                    ObservableList<String> horasPr = FXCollections.observableArrayList(horasPrueba);
                    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Usuario WHERE Nombre='" + u.getNombre() + "' AND (Rol=" + Rol + " OR Rol=0) ORDER BY Nombre, Rol DESC, Aula DESC, Horario DESC;");
                    ResultSet result = stmt.executeQuery();
                    if (result.isBeforeFirst()) {
                        if (visitados.containsKey(u.getNombre())) {
                            for (int i = visitados.get(u.getNombre()); i > 0; i--) {
                                result.next();
                            }
                        }
                        while (result.next()) {
                            String s = result.getString(3);
                            String s2 = result.getString(4);

                            if (s != null) {
                                comAula.setValue(s);
                                comHora.setValue(s2);
                                u.setAulaAnterior(s);
                                u.setHoraAnterior(s2);
                                if (visitados.containsKey(result.getString(1))) {
                                    visitados.put(result.getString(1), visitados.get(result.getString(1)) + 1);
                                } else {
                                    visitados.put(result.getString(1), 1);
                                }
                                break;
                            }
                        }
                    }
                    comAula.setItems(aulasPr);
                    if (comAula.getValue() == null) comAula.setValue("");
                    comHora.setItems(horasPr);
                    if (comHora.getValue() == null) comHora.setValue("");
                    if (Objects.equals(comHora.getValue(), "")) comAula.setDisable(true);
                    u.horaComboInicial = comHora.getValue();
                    u.aulaComboInicial = comAula.getValue();
                    u.setAulaBox(comAula);
                    u.setHoraBox(comHora);
                    usuarios.add(u);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return usuarios;
    }

    public void actualizarSede(Sede s, ResponsableSede r) {
        String sqlQuery;
        Statement stmt;
        if (r.getNombre().equals("")) {
            s.setRSedeAsignado(r);
            sqlQuery = "UPDATE Administracion SET Sede=null WHERE Sede='" + s.getNombre() + "';";
        } else {
            sqlQuery = "UPDATE Administracion SET Sede=null WHERE Sede='" + s.getNombre() + "';";
            try {
                stmt = connection.createStatement();
                stmt.executeUpdate(sqlQuery);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            s.setRSedeAsignado(r);
            sqlQuery = "UPDATE Administracion SET Sede='" + s.getNombre() + "' WHERE Usuario='" + r.getNombre() + "';";
        }

        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        controladorSede.cargar();
    }

    public void actualizarUsuario(Usuario u, Funcion f) {
        String sqlQuery;
        Statement stmt;
        if (f.getRol() == 0) {
            u.setFuncion(new Funcion(0, "", ""));
            sqlQuery = "UPDATE Usuario SET Rol=0 WHERE Nombre='" + u.getNombre() + "';";
        } else {
            u.setFuncion(f);
            String a = u.getAulaAnterior();
            String b = u.getHoraAnterior();
            if (u.getAulaAnterior().equals("")) {
                if (u.getHoraAnterior().equals("")) {
                    sqlQuery = "UPDATE Usuario SET Rol=" + f.getRol() + ", Aula='" + f.getAula() + "', Horario='" + f.getHora() + "' WHERE Nombre='" + u.getNombre() + "' AND Aula IS NULL AND Horario IS NULL;";
                } else {
                    sqlQuery = "UPDATE Usuario SET Rol=" + f.getRol() + ", Aula='" + f.getAula() + "', Horario='" + f.getHora() + "' WHERE Nombre='" + u.getNombre() + "' AND Aula IS NULL AND Horario='" + b + "';";
                }

            } else {
                if (u.getHoraAnterior().equals("")) {
                    sqlQuery = "UPDATE Usuario SET Rol=" + f.getRol() + ", Aula='" + f.getAula() + "', Horario='" + f.getHora() + "' WHERE Nombre='" + u.getNombre() + "' AND Horario IS NULL AND Aula='" + a + "';";
                } else {
                    sqlQuery = "UPDATE Usuario SET Rol=" + f.getRol() + ", Aula='" + f.getAula() + "', Horario='" + f.getHora() + "' WHERE Nombre='" + u.getNombre() + "' AND Aula='" + a + "' AND Horario='" + b + "';";
                }

            }


        }

        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(sqlQuery);
            if (u.getAulaAnterior().equals("") || u.getHoraAnterior().equals("")) {
                String sqlAnadir = "INSERT INTO Usuario (Nombre) Values ('" + u.getNombre() + "');";
                stmt.executeUpdate(sqlAnadir);
            }
            u.setAulaAnterior(f.getAula());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        controladorRAula.cargar();
        controladorVigilantes.cargar();
    }

    public void actualizarUsuario2(Usuario u, Funcion f) {
        String sqlQuery;
        PreparedStatement stmt;

        try {
            stmt = connection.prepareStatement("SELECT * FROM Usuario WHERE Nombre='" + u.getNombre() + "';");
            ResultSet result = stmt.executeQuery();
            result.next();
            if (result.next()) {
                sqlQuery = "DELETE FROM Usuario WHERE Nombre='" + u.getNombre() + "' AND Aula='" + f.getAula() + "' AND Horario='" + f.getHora() + "' LIMIT 1;";
                stmt.executeUpdate(sqlQuery);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        controladorRAula.cargar();
        controladorVigilantes.cargar();
    }

    public List<Sede> leerSedes() {
        String sqlQuery = "SELECT * FROM Sede";
        List<Sede> sedes = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    Sede res = new Sede(rs.getString(1));
                    List<ResponsableSede> usuarios = BD.getInstance().leerRespSedesLibres();
                    List<String> usuariosString = new ArrayList<>();
                    usuariosString.add("");
                    usuarios.forEach(u -> usuariosString.add(u.getNombre()));
                    ObservableList<String> usuariosObs = FXCollections.observableArrayList(usuariosString);
                    ComboBox<String> com = new ComboBox<>();
                    PreparedStatement stmt = connection.prepareStatement("SELECT Usuario FROM Administracion WHERE Sede='" + res.getNombre() + "';");
                    ResultSet result = stmt.executeQuery();
                    if (result.isBeforeFirst()) {
                        while (result.next()) {
                            String s = result.getString(1);
                            if (s != null) {
                                res.setRSedeAsignado(new ResponsableSede(s, null, null, null));
                                com.setValue(res.getRSedeAsignado().getNombre());
                            }
                        }
                    }

                    PreparedStatement stmt2 = connection.prepareStatement("SELECT t.*,sum(Aforo) FROM (SELECT DISTINCT Nombre,Aforo FROM Aula WHERE Sede='" + res.getNombre() + "')t;");
                    ResultSet result2 = stmt2.executeQuery();
                    if (result2.isBeforeFirst()) {
                        while (result2.next()) {
                            int s = result2.getInt(3);
                            res.setAforo(s);
                        }
                    }

                    com.setItems(usuariosObs);
                    res.setRSedeBox(com);
                    sedes.add(res);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return sedes;
    }

    public void cargarEstudiantes(String file) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            file = file.replaceAll("\\\\", "/");
        }

        String sqlQueryInstituto = "LOAD DATA LOCAL INFILE '" + file + "' IGNORE INTO TABLE Instituto FIELDS TERMINATED BY ';' LINES TERMINATED BY '\n' IGNORE 1 ROWS (Nombre)";
        String sqlQueryAsignacion = "LOAD DATA LOCAL INFILE '" + file + "' IGNORE INTO TABLE Asignacion FIELDS TERMINATED BY ';' LINES TERMINATED BY '\n' IGNORE 1 ROWS (Instituto)";
        String sqlQueryEstudiante = "LOAD DATA LOCAL INFILE '" + file + "' INTO TABLE Estudiante FIELDS TERMINATED BY ';' LINES TERMINATED BY '\n' IGNORE 1 ROWS (Centro, Nombre, PrimerApellido, SegundoApellido, Dni)";
        Statement stmt;

        try {
            stmt = connection.createStatement();
            stmt.execute(sqlQueryInstituto);
            stmt.execute(sqlQueryAsignacion);
            stmt.execute(sqlQueryEstudiante);

        } catch (SQLException e) {
            throw new BDException("Error: " + e);
        }
    }

    public void cargarMaterias() {
        String queryMateria = "INSERT IGNORE INTO Materia (Nombre, Horario) VALUES (?, ?)";
        String queryRealizacion = "INSERT INTO Realizacion (Materia, Aula) VALUES (?, NULL)";
        String line;

        try {
            PreparedStatement ps1 = connection.prepareStatement(queryMateria, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(queryRealizacion, PreparedStatement.RETURN_GENERATED_KEYS);

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("materias_pevau_con_franjas.csv")).getFile());

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    ps1.setString(1, line.split("; ")[0]);
                    ps1.setString(2, line.split("; ")[1]);
                    ps1.executeUpdate();

                    if (!buscarMateriaRealizacion(line.split("; ")[0])) {
                        ps2.setString(1, line.split("; ")[0]);
                        ps2.executeUpdate();
                    }
                }
            } catch (Exception e) {
                System.out.println("No existe el archivo: " + e);
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido cargar las materias: " + e);
        }
    }

    public boolean buscarMateriaRealizacion(String materia) {
        String query = "SELECT Materia FROM Realizacion GROUP BY Materia";
        boolean esta = false;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    String nombre = rs.getString(1);

                    if (materia.equals(nombre)) {
                        esta = true;
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return esta;
    }

    public List<Materia> materiasEstudiante(Estudiante e) {
        String sqlQuery = "SELECT Materia FROM Matriculacion WHERE Estudiante = '" + e.getDni() + "';";
        List<Materia> materias = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    String nombre = rs.getString(1);

                    materias.add(new Materia(nombre));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return materias;
    }

    public void leerMatriculaciones(String file) {
        BufferedWriter writer;
        BufferedReader br;
        String line;
        String[] cols;

        try {
            writer = new BufferedWriter(new FileWriter("matriculacion.csv"));
            br = new BufferedReader(new FileReader(file));

            br.readLine();

            while ((line = br.readLine()) != null) {
                cols = line.split(";");
                writer.write(cols[4] + ";" + cols[5] + "\n");
            }
            br.close();
            writer.close();

            br = new BufferedReader(new FileReader("matriculacion.csv"));
            writer = new BufferedWriter(new FileWriter("matriculacion2.csv"));

            while ((line = br.readLine()) != null) {
                String textoAsignaturas = line.replaceAll(".*?;", "");
                String dni = line.split(";")[0];
                String[] asignaturas = textoAsignaturas.split(",");

                for (int i = 0; i < asignaturas.length; i++) {
                    if (i != 0) {
                        asignaturas[i] = asignaturas[i].replaceFirst(" ", "");
                    }
                    writer.write(dni + ";" + asignaturas[i] + "\n");
                }
            }
            br.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String sqlQueryMatriculacion = "LOAD DATA LOCAL INFILE 'matriculacion2.csv' INTO TABLE Matriculacion FIELDS TERMINATED BY ';' LINES TERMINATED BY '\n' (Estudiante, Materia)";
        Statement stmt;

        try {
            stmt = connection.createStatement();
            stmt.execute(sqlQueryMatriculacion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void cargarRAulaVigi(String file) {
        String insertarUsuarios = "INSERT IGNORE INTO Usuario (Nombre,Rol,Aula,Horario) VALUES (?,0,null,null)";
        String line;
        String comprobarTabla = "SELECT * FROM Usuario";
        boolean x = true;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(comprobarTabla);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                x = false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (x) {
            try {
                PreparedStatement ps1 = connection.prepareStatement(insertarUsuarios, PreparedStatement.RETURN_GENERATED_KEYS);
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    while ((line = br.readLine()) != null) {
                        ps1.setString(1, line);
                        ps1.executeUpdate();
                    }
                    this.controladorRAula.cargar();
                    this.controladorVigilantes.cargar();
                } catch (Exception e) {
                    System.out.println("No existe el archivo: " + e);
                }
            } catch (SQLException e) {
                throw new BDException("No se ha podido cargar los usuarios");
            }
        }

    }

    public void cargarRespSedes() {
        String insertarUsuarios = "INSERT IGNORE INTO RespSedes (Nombre) VALUES (?)";
        String insertarAdministracion = "INSERT IGNORE INTO Administracion (Usuario) VALUES (?)";
        String line;

        try {
            PreparedStatement ps1 = connection.prepareStatement(insertarUsuarios, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(insertarAdministracion, PreparedStatement.RETURN_GENERATED_KEYS);
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("lista_usuarios.txt")).getFile());
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    ps1.setString(1, line);
                    ps2.setString(1, line);
                    ps1.executeUpdate();
                    ps2.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println("No existe el archivo: " + e);
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido cargar los responsables de sede");
        }
    }

    public void cargarSedes(String file) {
        String insertarSedes = "INSERT IGNORE INTO Sede (Nombre) VALUES (?)";
        String line;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertarSedes, PreparedStatement.RETURN_GENERATED_KEYS);

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                if (br.readLine().equals("SEDE")) {
                    while ((line = br.readLine()) != null) {
                        preparedStatement.setString(1, line.substring(6));
                        preparedStatement.executeUpdate();
                    }
                } else {
                    System.err.println("El archivo seleccionado no es válido");
                }
            } catch (Exception e) {
                System.out.println("No existe el archivo: " + e);
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido cargar las sedes");
        }
    }

    public void anadirSede(Sede sede) {
        String sqlQuery = "INSERT IGNORE INTO Sede (Nombre) VALUES (?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, sede.getNombre());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido añadir la sede");
        }
    }

    public void anadirAula(Aula aula) {
        String sqlQuery = "INSERT IGNORE INTO Aula (Nombre, Sede, Aforo, Horario,AforoD,HuecosLibres) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, aula.getNombre());
            preparedStatement.setString(2, aula.getSede().getNombre());
            preparedStatement.setInt(3, aula.getAforoTotal());
            preparedStatement.setString(4, aula.getHorario());
            preparedStatement.setInt(5,aula.getAforoTotal());
            preparedStatement.setInt(6,aula.getAforoTotal());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido añadir la sede");
        }
    }

    public void editarSede(String nuevo, String antiguo) {
        anadirSede(new Sede(nuevo));
        String adminQuery = "UPDATE Administracion SET Sede = ? WHERE (Sede = ?)";
        String sedeQuery = "DELETE FROM Sede WHERE (Nombre = ?)";

        try {
            PreparedStatement ps1 = connection.prepareStatement(adminQuery);
            PreparedStatement ps2 = connection.prepareStatement(sedeQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            ps1.setString(1, nuevo);
            ps1.setString(2, antiguo);
            ps1.executeUpdate();

            ps2.setString(1, antiguo);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido editar la sede");
        }
    }
    public void actualizarHuecosL(Aula aula, int huecos){
        String query = "UPDATE Aula SET HuecosLibres =? WHERE Nombre=? AND Horario=?;";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1,huecos);
            stmt.setString(2,aula.getNombre());
            stmt.setString(3,aula.getHorario());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void actualizarAforoD(Aula aula, int nuevoAforo){
        String query = "UPDATE Aula SET AforoD =? WHERE Nombre=? AND Horario=?;";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1,nuevoAforo);
            stmt.setString(2,aula.getNombre());
            stmt.setString(3,aula.getHorario());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void editarAula(Aula nuevo, String antiguo, String horario) {
        String adminQuery = "UPDATE Aula SET Nombre = ?, Sede = ?, Aforo = ?, Horario = ? WHERE (Nombre = ? AND Horario = ?)";

        try {
            PreparedStatement ps1 = connection.prepareStatement(adminQuery);

            ps1.setString(1, nuevo.getNombre());
            ps1.setString(2, nuevo.getSede().getNombre());
            ps1.setInt(3, nuevo.getAforoTotal());
            ps1.setString(4, nuevo.getHorario());
            ps1.setString(5, antiguo);
            ps1.setString(6, horario);

            ps1.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido editar el aula");
        }
    }

    public void eliminarSede(Sede sede) {
        String adminQuery = "DELETE FROM Administracion WHERE (Sede = ?)";
        String sedeQuery = "DELETE FROM Sede WHERE (Nombre = ?)";

        try {
            PreparedStatement ps1 = connection.prepareStatement(adminQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(sedeQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            ps1.setString(1, sede.getNombre());
            ps2.setString(1, sede.getNombre());

            ps1.executeUpdate();
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido eliminar la sede");
        }
    }

    public List<Instituto> leerInstitutos() {
        String query = "SELECT * FROM Instituto";
        List<Instituto> institutos = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        String nombre = rs.getString(1);

                        institutos.add(new Instituto(nombre));
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer los institutos: " + e);
        }

        return institutos;
    }

    public void actualizarInstituto(Instituto instituto, Sede sede) {
        String query = "UPDATE Asignacion SET Sede = null WHERE Instituto = ?;";
        PreparedStatement stmt;
        if (!sede.getNombre().equals("")) {
            try {
                stmt = connection.prepareStatement(query);
                stmt.setString(1, instituto.getNombre());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new BDException("Error al actualizar los institutos: " + e);
            }

            query = "UPDATE Asignacion SET Sede = '" + sede.getNombre() + "' WHERE Instituto = ?;";
        }

        try {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, instituto.getNombre());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("Error al actualizar los institutos: " + e);
        }
    }

    public List<Sede> leerTodasSedes() {
        String query = "SELECT * FROM Sede";
        List<Sede> sedes = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        String nombre = rs.getString(1);

                        sedes.add(new Sede(nombre));
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer las sedes: " + e);
        }

        return sedes;
    }

    public int numAlumnos(Instituto i) {
        String query = "SELECT count(Nombre) FROM Estudiante WHERE Centro=?;";
        int num = 0;
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, i.getNombre());
            ResultSet rs = stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    num = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return num;
    }

    public List<Sede> sedesPosibles(int aforo) {
        String enUnaSede = "SELECT Instituto FROM Asignacion WHERE Sede=?";
        String query = "SELECT Sede,sum(Aforo) FROM Aula group by Sede having sum(Aforo)>=" + aforo + ";";
        List<Sede> sedesPosibles = new ArrayList<>();
        List<Sede> sedesConHueco = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    Sede a = new Sede(rs.getString(1));
                    a.setAforo(rs.getInt(2));
                    sedesPosibles.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int num = 0;
        for (Sede s : sedesPosibles) {
            try {
                PreparedStatement stmt2 = connection.prepareStatement(enUnaSede);
                stmt2.setString(1, s.toString());
                ResultSet rs = stmt2.executeQuery();
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        num += numAlumnos(new Instituto(rs.getString(1)));
                    }
                }
                if (s.getAforo() - num >= aforo) {
                    sedesConHueco.add(s);
                }
                num = 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sedesConHueco;
    }

    public List<Aula> leerAulas() {
        String query = "SELECT * FROM Aula";
        List<Aula> aulas = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        String nombre = rs.getString(1);
                        String sede = rs.getString(2);
                        int aforo = rs.getInt(3);
                        String horario = rs.getString(4);
                        int aforoD = rs.getInt(5);
                        int huecosL=rs.getInt(6);

                        aulas.add(new Aula(nombre, new Sede(sede), aforo, horario, aforoD,huecosL));
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer las sedes: " + e);
        }

        return aulas;
    }

    public List<Aula> leerAulasPorSede(Sede seleccionada, Materia materia) {
        String query = "SELECT * FROM Aula A INNER JOIN Realizacion R WHERE A.Horario = '" + materia.getHorario() + "' AND R.Materia = '" + materia.getNombre() + "' AND A.Sede = '" + seleccionada.getNombre() + "' GROUP BY A.Nombre";
        List<Aula> aulas = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        String nombre = rs.getString(1);
                        String sede = rs.getString(2);
                        int aforo = rs.getInt(3);
                        String horario = rs.getString(4);
                        int aforoD=rs.getInt(5);

                        aulas.add(new Aula(nombre, new Sede(sede), aforo, horario, aforoD));
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer las aulas por sede: " + e);
        }

        return aulas;
    }

    public void eliminarAula(Aula aula) {
        String aulaQuery = "DELETE FROM Aula WHERE (Nombre = ? AND Horario = ?)";

        try {
            PreparedStatement ps1 = connection.prepareStatement(aulaQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            ps1.setString(1, aula.getNombre());
            ps1.setString(2, aula.getHorario());

            ps1.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido eliminar el aula");
        }
    }

    public Sede leerAsignaciones(Instituto instituto) {
        String nombre = instituto.getNombre();

        if (nombre.contains("'")) {
            nombre = nombre.replace("'", "\\'");
        }

        String query = "SELECT Sede FROM Asignacion WHERE Instituto = '" + nombre + "';";
        Sede seleccionada = null;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    String n = rs.getString(1);

                    if (n != null) {
                        seleccionada = new Sede(n);
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer las sedes: " + e);
        }

        return seleccionada;
    }

    public List<Materia> leerMaterias() {
        String query = "SELECT * FROM Materia";
        List<Materia> materias = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        String nombre = rs.getString(1);
                        String horario = rs.getString(2);

                        materias.add(new Materia(nombre, horario));
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer las sedes: " + e);
        }

        return materias;
    }

    public List<Aula> leerRealizaciones(Materia materia) {
        String query = "SELECT Aula FROM Realizacion WHERE Materia = '" + materia.getNombre() + "';";
        List<Aula> aulas = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    String n = rs.getString(1);

                    if (n != null) {
                        aulas.add(new Aula(n));
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer las aulas: " + e);
        }

        return aulas;
    }

    public void actualizarExamen(Materia materia, List<Aula> aulas) {
        Statement statement;
        PreparedStatement preparedStatement;

        for (Aula aula : aulas) {
            if (!buscarRealizacion(materia, aula)) {
                try {
                    statement = connection.createStatement();
                    statement.execute("UPDATE IGNORE Realizacion SET Aula = '" + aula.getNombre() + "' WHERE Materia = '" + materia.getNombre() + "' AND Aula IS NULL;");
                } catch (SQLException e) {
                    throw new BDException("No se ha podido actualizar los exámenes: " + e);
                }
            }
        }

        List<String> nombres = aulas.stream()
                .map(Aula::getNombre)
                .collect(Collectors.toList());

        try {
            preparedStatement = connection.prepareStatement("DELETE FROM Realizacion WHERE Materia = '" + materia.getNombre() + "' AND Aula NOT IN ('" + String.join("', '", nombres) + "')");
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("DELETE FROM Realizacion WHERE Materia = '" + materia.getNombre() + "' AND Aula IS NULL");
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("INSERT INTO Realizacion (Materia, Aula) VALUES (?, NULL)", PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, materia.getNombre());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("No se ha podido actualizar los exámenes: " + e);
        }
    }

    private boolean buscarRealizacion(Materia materia, Aula aula) {
        String query = "SELECT * FROM Realizacion";
        boolean esta = false;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    String n = rs.getString(1);
                    String a = rs.getString(2);

                    if (materia.getNombre().equals(n) && aula.getNombre().equals(a)) {
                        esta = true;
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return esta;
    }

    public void cargarRegistro(String file) {
        String insertarSedes = "INSERT IGNORE INTO Registro (Fecha, Log) VALUES (?, ?)";
        String line;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertarSedes, PreparedStatement.RETURN_GENERATED_KEYS);

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    preparedStatement.setString(2, line);
                    preparedStatement.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println("No existe el archivo: " + e);
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido cargar el registro");
        }
    }

    public List<String> leerRegistro() {
        String query = "SELECT Fecha, Log FROM Registro";
        List<String> registro = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        Timestamp fecha = rs.getTimestamp(1);
                        String log = rs.getString(2);

                        registro.add("(" + dateFormat.format(fecha) + ") " + log);
                    }
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer el registro: " + e);
        }

        return registro;
    }

    public void anadirEstudiante(Estudiante estudiante) {
        String queryEstudiante = "INSERT IGNORE INTO Estudiante (Centro, Nombre, PrimerApellido, SegundoApellido, Dni) VALUES (?,?,?,?,?)";
        String queryMatriculacion = "INSERT IGNORE INTO Matriculacion (Estudiante, Materia) VALUES (?, ?)";

        try {
            PreparedStatement ps1 = connection.prepareStatement(queryEstudiante, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(queryMatriculacion, PreparedStatement.RETURN_GENERATED_KEYS);

            ps1.setString(1, estudiante.getCentro().getNombre());
            ps1.setString(2, estudiante.getNombre());
            ps1.setString(3, estudiante.getPrimerApellido());
            ps1.setString(4, estudiante.getSegundoApellido());
            ps1.setString(5, estudiante.getDni());

            ps1.executeUpdate();

            for (Materia materia : estudiante.getMaterias()) {
                ps2.setString(1, estudiante.getDni());
                ps2.setString(2, materia.getNombre());

                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido añadir el estudiante");
        }
    }

    public void eliminarRegistro(String registro) {
        String[] log = registro.split("\\)\\s");
        String query = "DELETE FROM Registro WHERE Log = '" + log[1] + "';";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BDException("Error al eliminar el registro");
        }
    }

    public int contarMatriculasPorMateriaYSede(Materia materia, Sede sede) {
        String query = "SELECT COUNT(E.Dni) FROM Estudiante E, Matriculacion M WHERE E.Dni = M.Estudiante AND M.Materia = '" + materia.getNombre() + "' AND E.Centro IN (SELECT Instituto FROM Asignacion WHERE Sede = '" + sede.getNombre() + "');";
        int res = 0;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    res = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new BDException("No se ha podido leer el número de matriculados por materia y sede: " + e);
        }

        return res;
    }

    public void actualizarNumAsignados(Materia m, Aula aula, int num){
        String query = "INSERT INTO NumAsignados VALUES (?,?,?);";
        String borrar="DELETE FROM NumAsignados WHERE Materia=? AND Aula=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(borrar);
            stmt.setString(2,aula.getNombre());
            stmt.setString(1,m.getNombre());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1,aula.getNombre());
            stmt.setString(2,m.getNombre());
            stmt.setInt(3,num);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int obtenerNumAsignados(Materia m, Aula aula){
        String query = "SELECT numAsignados FROM NumAsignados WHERE Materia=? AND Aula=?";
        int res=0;
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(2,aula.getNombre());
            stmt.setString(1,m.getNombre());
            ResultSet rs=stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    res = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;

    }
    public int obtenerHuecosLibres(Aula aula, String horario){
        String query = "SELECT huecosLibres FROM Aula WHERE Nombre=? AND Horario=?";
        int res=0;
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1,aula.getNombre());
            stmt.setString(2,horario);
            ResultSet rs=stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    res = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;

    }
    public int obtenerNumAsignados(Materia m, Sede sede){
        String query = "SELECT sum(numAsignados) FROM NumAsignados WHERE Materia=? AND Aula IN (SELECT Nombre FROM Aula WHERE Sede=?)";
        int res=0;
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(2,sede.getNombre());
            stmt.setString(1,m.getNombre());
            ResultSet rs=stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    res = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;

    }
}