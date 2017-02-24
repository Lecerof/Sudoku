
import java.beans.*;
import java.io.*;

public interface Model extends Serializable {
  void addPropertyChangeListener(PropertyChangeListener l);
  void removePropertyChangeListener(PropertyChangeListener l); 
}

