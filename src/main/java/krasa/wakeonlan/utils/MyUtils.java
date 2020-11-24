package krasa.wakeonlan.utils;

import javafx.scene.image.*;
import krasa.wakeonlan.*;

import java.util.*;

public class MyUtils {


	public static Image getImage(String name) {
		return new Image(Objects.requireNonNull(JavaFxApplication.class.getResourceAsStream(name)));
	}
}
