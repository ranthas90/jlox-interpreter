package virtualmachine.vm;

// TODO: tenemos un Upvalue en el paquete compiler y este, hay que buscar nombres mejores
public class Upvalue {

    private Object closedValue;
    private int location; // TODO: en el libro esto es un puntero al valor de la variable "closure"
    private Upvalue next;

    public Upvalue(int location) {
        this.location = location;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public Upvalue getNext() {
        return next;
    }

    public void setNext(Upvalue next) {
        this.next = next;
    }

    public Object getClosedValue() {
        return closedValue;
    }

    public void setClosedValue(Object closedValue) {
        this.closedValue = closedValue;
    }
}
