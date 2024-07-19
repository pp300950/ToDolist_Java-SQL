import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ToDoListApp extends Application {

    private static final String URL = "jdbc:mysql://localhost:3306/todolist";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection conn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("เชื่อมต่อกับmysqlสำเร็จ");
        } catch (SQLException e) {
            showError("เกิดข้อผิดพลาดในการเชื่อมต่อกับmySQL: " + e.getMessage());
            return;
        }

        primaryStage.setTitle("ToDo List App");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        GridPane addTaskPane = new GridPane();
        addTaskPane.setHgap(10);
        addTaskPane.setVgap(10);

        Label nameLabel = new Label("ชื่อ: ");
        TextField nameField = new TextField();
        Label descLabel = new Label("คำอธิบาย: ");
        TextField descField = new TextField();
        Label statusLabel = new Label("สถานะ: ");
        TextField statusField = new TextField();
        Button addButton = new Button("เพิ่มงาน");

        addTaskPane.add(nameLabel, 0, 0);
        addTaskPane.add(nameField, 1, 0);
        addTaskPane.add(descLabel, 0, 1);
        addTaskPane.add(descField, 1, 1);
        addTaskPane.add(statusLabel, 0, 2);
        addTaskPane.add(statusField, 1, 2);
        addTaskPane.add(addButton, 1, 3);
        addButton.setOnAction(e -> addTask(nameField.getText(), descField.getText(), statusField.getText()));

        vbox.getChildren().add(addTaskPane);
        ListView<String> taskListView = new ListView<>();
        Button refreshButton = new Button("รีเฟรชรายการ");
        refreshButton.setOnAction(e -> readAllTasks(taskListView));
        vbox.getChildren().addAll(taskListView, refreshButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        readAllTasks(taskListView);
    }

    private void addTask(String taskName, String taskDescription, String taskStatus) {
        String sql = "INSERT INTO tasks (task_name, task_description, task_status) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, taskName);
            statement.setString(2, taskDescription);
            statement.setString(3, taskStatus);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                showInfo("เพิ่มงาน \"" + taskName + "\" สำเร็จแล้ว!");
                
            }
        } catch (SQLException e) {
            showError("เกิดข้อผิดพลาด: " + e.getMessage());
            
        }
    }

    private void readAllTasks(ListView<String> taskListView) {
        taskListView.getItems().clear();
        String sql = "SELECT * FROM tasks";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_description");
                String taskStatus = resultSet.getString("task_status");
                taskListView.getItems().add("ID: " + id + ", ชื่องาน: " + taskName + ", คำอธิบาย: " + taskDescription + ", สถานะ: " + taskStatus);
            }
        } catch (SQLException e) {

            showError("เกิดข้อผิดพาด: " + e.getMessage());

        }
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        super.stop();
    }
}
