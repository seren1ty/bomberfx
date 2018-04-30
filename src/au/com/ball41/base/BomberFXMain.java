package au.com.ball41.base;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BomberFXMain extends Application {

	@Override
	public void start(Stage inPrimaryStage) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("view/BomberFXView.fxml"));
        
		inPrimaryStage.setScene(new Scene(root));
		inPrimaryStage.show();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
