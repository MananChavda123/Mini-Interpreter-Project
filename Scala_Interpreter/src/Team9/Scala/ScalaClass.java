package Team9.Scala;

import java.util.List;
import java.util.Map;

public class ScalaClass implements ScalaCallable {
    final String name;
    final ScalaClass superclass;
    private final Map<String, ScalaFunction> methods;

    ScalaClass(String name, ScalaClass superclass, Map<String, ScalaFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    ScalaFunction findMethod(ScalaInstance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        if (superclass != null) {
            return superclass.findMethod(instance, name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        ScalaInstance instance = new ScalaInstance(this);

        ScalaFunction initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        ScalaFunction initializer = methods.get("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }
	
}
