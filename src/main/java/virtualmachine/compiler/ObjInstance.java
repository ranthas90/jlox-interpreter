package virtualmachine.compiler;

import java.util.HashMap;
import java.util.Map;

public class ObjInstance {

    private ObjClass clazz;
    private Map<String, Object> fields;

    public ObjInstance(ObjClass clazz) {
        this.clazz = clazz;
        this.fields = new HashMap<>();
    }

    public ObjClass getClazz() {
        return clazz;
    }

    public Object getFieldByName(String fieldName) {
        return fields.get(fieldName);
    }

    public void addValueToField(String fieldName, Object value) {
        fields.put(fieldName, value);
    }

    @Override
    public String toString() {
        return String.format("%s instance", clazz.getName());
    }
}
