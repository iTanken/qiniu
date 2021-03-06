/**
 * 
 */
package com.zhazhapan.qiniu.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.zhazhapan.qiniu.QiniuApplication;
import com.zhazhapan.qiniu.QiManager.FileAction;
import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.controller.MainWindowController;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;

/**
 * @author pantao 对JavaFX对话框进行封装
 */
public class Dialogs {

	private Logger logger = Logger.getLogger(Dialogs.class);

	public static String showInputDialog(String header, String content, String defaultValue) {
		TextInputDialog dialog = new TextInputDialog(defaultValue);
		dialog.setTitle(Values.MAIN_TITLE);
		dialog.setHeaderText(header);
		dialog.setContentText(content);

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}

	public Pair<FileAction, String[]> showFileMovableDialog(String bucket, String key, boolean setKey) {
		MainWindowController main = MainWindowController.getInstance();
		ButtonType ok = new ButtonType(Values.OK, ButtonData.OK_DONE);
		Dialog<String[]> dialog = getDialog(ok);

		TextField keyTextField = new TextField();
		keyTextField.setPrefWidth(300);
		keyTextField.setPromptText(Values.FILE_NAME);
		keyTextField.setText(key);
		ComboBox<String> bucketCombo = new ComboBox<String>();
		bucketCombo.getItems().addAll(main.bucketChoiceCombo.getItems());
		bucketCombo.setValue(bucket);
		CheckBox copyasCheckBox = new CheckBox(Values.COPY_AS);
		copyasCheckBox.setSelected(true);

		GridPane grid = getGridPane();
		grid.add(copyasCheckBox, 0, 0, 2, 1);
		grid.add(new Label(Values.BUCKET_NAME), 0, 1);
		grid.add(bucketCombo, 1, 1);
		if (setKey) {
			grid.add(new Label(Values.FILE_NAME), 0, 2);
			grid.add(keyTextField, 1, 2);
			Platform.runLater(() -> keyTextField.requestFocus());
		}

		dialog.getDialogPane().setContent(grid);
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ok) {
				return new String[] { bucketCombo.getValue(), keyTextField.getText() };
			}
			return null;
		});

		Optional<String[]> result = dialog.showAndWait();
		if (result.isPresent()) {
			bucket = bucketCombo.getValue();
			key = keyTextField.getText();
			FileAction action = copyasCheckBox.isSelected() ? FileAction.COPY : FileAction.MOVE;
			return new Pair<FileAction, String[]>(action, new String[] { bucket, key });
		} else {
			return null;
		}
	}

	/**
	 * 显示输入密钥的对话框
	 * 
	 * @return 返回用户是否点击确定按钮
	 */
	public boolean showInputKeyDialog() {
		ButtonType ok = new ButtonType(Values.OK, ButtonData.OK_DONE);
		Dialog<String[]> dialog = getDialog(ok);

		TextField ak = new TextField();
		ak.setMinWidth(400);
		ak.setPromptText("Access Key");
		TextField sk = new TextField();
		sk.setPromptText("Secret Key");

		Hyperlink hyperlink = new Hyperlink("查看我的KEY：" + Values.QINIU_KEY_URL);
		hyperlink.setOnAction(event -> Utils.openLink(Values.QINIU_KEY_URL));

		GridPane grid = getGridPane();
		grid.add(hyperlink, 0, 0, 2, 1);
		grid.add(new Label("Access Key:"), 0, 1);
		grid.add(ak, 1, 1);
		grid.add(new Label("Secret Key:"), 0, 2);
		grid.add(sk, 1, 2);

		Node okButton = dialog.getDialogPane().lookupButton(ok);
		okButton.setDisable(true);

		// 监听文本框的输入状态
		ak.textProperty().addListener((observable, oldValue, newValue) -> {
			okButton.setDisable(newValue.trim().isEmpty() || sk.getText().trim().isEmpty());
		});
		sk.textProperty().addListener((observable, oldValue, newValue) -> {
			okButton.setDisable(newValue.trim().isEmpty() || ak.getText().trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> ak.requestFocus());

		Optional<String[]> result = dialog.showAndWait();
		if (result.isPresent() && Checker.isNotEmpty(ak.getText()) && Checker.isNotEmpty(sk.getText())) {
			ConfigLoader.writeKey(ak.getText(), sk.getText());
			return true;
		}
		return false;
	}

	public void showBucketAddableDialog() {
		ButtonType ok = new ButtonType(Values.OK, ButtonData.OK_DONE);
		Dialog<String[]> dialog = getDialog(ok);

		TextField bucket = new TextField();
		bucket.setPromptText(Values.BUCKET_NAME);
		TextField url = new TextField();
		url.setPromptText(Values.BUCKET_URL);
		// TextField zone = new TextField();
		ComboBox<String> zone = new ComboBox<String>();
		zone.getItems().addAll(Values.BUCKET_NAME_ARRAY);
		zone.setValue(Values.BUCKET_NAME_ARRAY[0]);

		GridPane grid = getGridPane();
		grid.add(new Label(Values.BUCKET_NAME), 0, 0);
		grid.add(bucket, 1, 0);
		grid.add(new Label(Values.BUCKET_URL), 0, 1);
		grid.add(url, 1, 1);
		grid.add(new Label(Values.BUCKET_ZONE_NAME), 0, 2);
		grid.add(zone, 1, 2);

		Node okButton = dialog.getDialogPane().lookupButton(ok);
		okButton.setDisable(true);

		// 监听文本框的输入状态
		bucket.textProperty().addListener((observable, oldValue, newValue) -> {
			okButton.setDisable(newValue.trim().isEmpty() || url.getText().isEmpty());
		});
		url.textProperty().addListener((observable, oldValue, newValue) -> {
			okButton.setDisable(newValue.trim().isEmpty() || bucket.getText().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> bucket.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ok) {
				return new String[] { bucket.getText(),
						zone.getValue() + " " + (Checker.isHyperLink(url.getText()) ? url.getText() : "example.com") };
			}
			return null;
		});

		Optional<String[]> result = dialog.showAndWait();
		result.ifPresent(res -> {
			logger.info("bucket name: " + res[0] + ", zone name: " + res[1]);
			Platform.runLater(() -> MainWindowController.getInstance().addItem(res[0]));
			QiniuApplication.buckets.put(res[0], res[1]);
			ConfigLoader.writeConfig();
		});
	}

	public GridPane getGridPane() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		return grid;
	}

	public Dialog<String[]> getDialog(ButtonType ok) {
		Dialog<String[]> dialog = new Dialog<String[]>();
		dialog.setTitle(Values.MAIN_TITLE);
		dialog.setHeaderText(null);

		dialog.initModality(Modality.APPLICATION_MODAL);

		// 自定义确认和取消按钮
		ButtonType cancel = new ButtonType(Values.CANCEL, ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);
		return dialog;
	}

	public static Optional<ButtonType> showInformation(String content) {
		return showInformation(null, content);
	}

	public static Optional<ButtonType> showInformation(String header, String content) {
		return alert(header, content, AlertType.INFORMATION);
	}

	public static Optional<ButtonType> showWarning(String content) {
		return showWarning(null, content);
	}

	public static Optional<ButtonType> showWarning(String header, String content) {
		return alert(header, content, AlertType.WARNING);
	}

	public static Optional<ButtonType> showError(String content) {
		return showError(null, content);
	}

	public static Optional<ButtonType> showError(String header, String content) {
		return alert(header, content, AlertType.ERROR);
	}

	public static Optional<ButtonType> showConfirmation(String content) {
		return showConfirmation(null, content);
	}

	public static Optional<ButtonType> showConfirmation(String header, String content) {
		return alert(header, content, AlertType.CONFIRMATION);
	}

	public static Optional<ButtonType> showException(Exception e) {
		return showException(null, e);
	}

	public static void showFatalError(String header, Exception e) {
		showException(header, e);
		System.exit(0);
	}

	public static Optional<ButtonType> showException(String header, Exception e) {
		Alert alert = getAlert(header, "错误信息追踪：", AlertType.ERROR);

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		String exception = stringWriter.toString();

		TextArea textArea = new TextArea(exception);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(textArea, 0, 0);

		alert.getDialogPane().setExpandableContent(gridPane);

		return alert.showAndWait();
	}

	public static Optional<ButtonType> alert(String content) {
		return alert(null, content);
	}

	public static Optional<ButtonType> alert(String content, AlertType alertType) {
		return alert(null, content, alertType);
	}

	public static Optional<ButtonType> alert(String header, String content) {
		return alert(header, content, AlertType.INFORMATION);
	}

	public static Optional<ButtonType> alert(String header, String content, AlertType alertType) {
		return alert(header, content, alertType, Modality.NONE, null, StageStyle.DECORATED);
	}

	public static Optional<ButtonType> alert(String header, String content, AlertType alertType, Modality modality,
			Window window, StageStyle style) {
		return getAlert(header, content, alertType, modality, window, style).showAndWait();
	}

	public static Alert getAlert(String header, String content, AlertType alertType) {
		return getAlert(header, content, alertType, Modality.APPLICATION_MODAL, null, StageStyle.DECORATED);
	}

	public static Alert getAlert(String header, String content, AlertType alertType, Modality modality, Window window,
			StageStyle style) {
		Alert alert = new Alert(alertType);

		alert.setTitle(Values.MAIN_TITLE);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.initModality(modality);
		alert.initOwner(window);
		alert.initStyle(style);

		return alert;
	}
}
