package Team9.Scala;

import java.util.List;

public interface ScalaCallable {
	 int arity();
	 Object call(Interpreter interpreter, List<Object> arguments);
}
