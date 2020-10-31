package krasa.wakeonlan;

import javafx.scene.image.Image;

import java.util.Objects;

public class MyUtils {


	public static Image getImage(String name) {
		return new Image(Objects.requireNonNull(JavaFxApplication.class.getResourceAsStream(name)));
	}
}
