package krasa.wakeonlan.utils;

import javafx.scene.*;
import javafx.stage.*;

public class UiUtils {
	public static Stage getStage(Node node) {
		return  (Stage) node.getScene().getWindow();
	}
}
