package virtualmachine.vm;

// TODO: tenemos un Upvalue en el paquete compiler y este, hay que buscar nombres mejores
public class Upvalue {

    private Object closureValue;
    private int location; // TODO: en el libro esto es un puntero al valor de la variable "closure"

    public Upvalue(int location) {
        this.location = location;
    }

    public int getLocation() {
        return location;
    }
}
