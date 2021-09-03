import java.io.Serializable;
public class Usuario implements Serializable {
    
    int difficultad;
    String nombre;
    int edad;
    
    
    public Usuario( int difficultad, String nombre, int edad) {
        this.difficultad = difficultad;
        this.nombre = nombre;
        this.edad = edad;

    }
    public int getDif() {
        return difficultad;
    }
    public String getNombre() {
        return nombre;
    }
    public int getEdad() {
        return edad;
    }
}