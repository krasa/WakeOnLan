package krasa.wakeonlan;

import javafx.scene.Node;
import javafx.stage.Stage;

public class UiUtils {
	public static Stage getStage(Node node) {
		return  (Stage) node.getScene().getWindow();
	}
}
