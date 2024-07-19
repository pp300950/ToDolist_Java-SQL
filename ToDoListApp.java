import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ToDoListApp extends Application {

    private static final String URL = "jdbc:mysql://localhost:3306/todolist";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection conn;
    private ListView<String> taskListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("เชื่อมต่อกับ mysql เเล้ว");
        } catch (SQLException e) {

            showError("เกิดข้อผิดพลาดในการเชื่อมต่อกับ MySQL: " + e.getMessage());
            return;

        }

        primaryStage.setTitle("ToDo List App");
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        GridPane addTaskPane = new GridPane();
        addTaskPane.setHgap(10);
        addTaskPane.setVgap(10);

        //ช่องใส่ชื่อ
        Label nameLabel = new Label("ชื่อ: ");
        TextField nameField = new TextField();

        //คำอธิบา
        Label descLabel = new Label("คำอธิบาย: ");
        TextField descField = new TextField();

        //สถานะของงานComboBox
        Label statusLabel = new Label("สถานะ: ");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("ยังไม่เริ่ม", "กำลังทำ", "สำเร็จแล้ว");
        statusComboBox.setValue("ยังไม่เริ่ม");

        //สร้างปุ่มเพิ่มงาน
        Button addButton = new Button("เพิ่มงาน");
        addTaskPane.add(nameLabel, 0, 0);
        addTaskPane.add(nameField, 1, 0);
        addTaskPane.add(descLabel, 0, 1);
        addTaskPane.add(descField, 1, 1);
        addTaskPane.add(statusLabel, 0, 2);
        addTaskPane.add(statusComboBox, 1, 2);
        addTaskPane.add(addButton, 1, 3);

        //หลังกดปุ่เพิ่มงาน
        addButton.setOnAction(e -> {

            addTask(nameField.getText(), descField.getText(), statusComboBox.getValue());
            nameField.clear();
            descField.clear();
            statusComboBox.setValue("ยังไม่เริ่ม");

            readAllTasks();

        });

        vbox.getChildren().add(addTaskPane);
        taskListView = new ListView<>();
        Button refreshButton = new Button("รีเฟรชรายการ");

        //รีเฟชรค่าหลังกดปุ่ม
        refreshButton.setOnAction(e -> readAllTasks());
        vbox.getChildren().addAll(taskListView, refreshButton);

        //ปุ่มลบงาน
        Button deleteButton = new Button("ลบงาน");
        deleteButton.setOnAction(e -> {

            String selectedItem = taskListView.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {

                int id = Integer.parseInt(selectedItem.split(",")[0].split(":")[1].trim());
                deleteTask(id);
                readAllTasks();

            }
        });

        //ปุ่มเพิ่มขนาดฟอน
        Button increaseFontButton = new Button("เพิ่มขนาดฟอนต์");
        increaseFontButton.setOnAction(e -> changeFontSize(2));

        //ปุ่มลดขนาดฟอน
        Button decreaseFontButton = new Button("ลดขนาดฟอนต์");
        decreaseFontButton.setOnAction(e -> changeFontSize(-2));

        //จัดให้ปุ่อยู่บรรทัดเดียวกันเลย
        HBox fontButtonBox = new HBox(10, increaseFontButton, decreaseFontButton);
        vbox.getChildren().addAll(deleteButton, fontButtonBox);

        //เเก้ไขสถานกล่องขเ้อมูล
        taskListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedItem = taskListView.getSelectionModel().getSelectedItem();

                if (selectedItem != null) {
                    int id = Integer.parseInt(selectedItem.split(",")[0].split(":")[1].trim());
                    showEditDialog(id);

                }
            }
        });

        Scene scene = new Scene(vbox, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        readAllTasks();
    }

    // ฟังก์ชั่นเพิ่มงานใหม่ในฐานข้อมูล
    private void addTask(String taskName, String taskDescription, String taskStatus) {
        String sql = "INSERT INTO tasks (task_name, task_description, task_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, taskName);
            statement.setString(2, taskDescription);
            statement.setString(3, taskStatus);

            Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
            statement.setTimestamp(4, currentTimestamp);
            statement.setTimestamp(5, currentTimestamp);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                showInfo("เพิ่มงาน \"" + taskName + "\" สำเร็จแล้ว!");
            }
        } catch (SQLException e) {
            showError("เกิดข้อผิดพลาด: " + e.getMessage());

        }

    }

    // ฟังก์ชั่นลบงานจากฐานข้อมูล
    private void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                showInfo("ลบงานสำเร็จแล้ว!");
            }
        } catch (SQLException e) {
            //เเก้บัค
            showError("เกิดข้อผิดพลาด: " + e.getMessage());
        }

    }

    // ฟังก์ชั่นอ่านข้อมูลทั้งหมดจากฐานข้อมูล
    private void readAllTasks() {
        taskListView.getItems().clear();
        String sql = "SELECT * FROM tasks";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_description");
                String taskStatus = resultSet.getString("task_status");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                
                taskListView.getItems().add("ID: " + id + "| ชื่องาน: " + taskName + "| คำอธิบาย: " + taskDescription + ", สถานะ: " + taskStatus + ", สร้างเมื่อ: " + createdAt + ", อัปเดตล่าสุด: " + updatedAt);
            }
        } catch (SQLException e) {

            showError("เกิดข้อผิดพลาด: " + e.getMessage());
        }

    }

    //สำหรับการเปลี่ยนขนาดฟอน์
    private void changeFontSize(int delta) {

        double currentFontSize = taskListView.getStyle().contains("-fx-font-size")
                ? Double.parseDouble(taskListView.getStyle().split("-fx-font-size: ")[1].replace("px;", ""))
                : 12.0;
        double newFontSize = currentFontSize + delta;
        taskListView.setStyle("-fx-font-size: " + newFontSize + "px;");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ข้อผิดพลาด");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //เเสดงข้อมูล
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ข้อมูล");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //หนา้ต่างเเก้งาน
    private void showEditDialog(int taskId) {
        try {
            String sql = "SELECT * FROM tasks WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, taskId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_description");
                String taskStatus = resultSet.getString("task_status");

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("แก้ไขงาน");

                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(20, 150, 10, 10));

                TextField nameField = new TextField(taskName);
                TextField descField = new TextField(taskDescription);
                ComboBox<String> statusComboBox = new ComboBox<>();
                statusComboBox.getItems().addAll("ยังไม่เริ่ม", "กำลังทำ", "สำเร็จแล้ว");
                statusComboBox.setValue(taskStatus);

                gridPane.add(new Label("ชื่อ:"), 0, 0);
                gridPane.add(nameField, 1, 0);
                gridPane.add(new Label("คำอธิบาย:"), 0, 1);
                gridPane.add(descField, 1, 1);
                gridPane.add(new Label("สถานะ:"), 0, 2);
                gridPane.add(statusComboBox, 1, 2);

                dialog.getDialogPane().setContent(gridPane);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                dialog.setResultConverter(dialogButton -> {

                    if (dialogButton == ButtonType.OK) {

                        try {
                            String updateSql = "UPDATE tasks SET task_name = ?, task_description = ?, task_status = ?, updated_at = ? WHERE id = ?";
                            PreparedStatement updateStatement = conn.prepareStatement(updateSql);
                            updateStatement.setString(1, nameField.getText());
                            updateStatement.setString(2, descField.getText());
                            updateStatement.setString(3, statusComboBox.getValue());
                            updateStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                            updateStatement.setInt(5, taskId);
                            updateStatement.executeUpdate();
                            readAllTasks();
                            return ButtonType.OK;

                        } catch (SQLException e) {

                            showError("เกิดข้อผิดพลาด: " + e.getMessage());

                        }
                    }
                    return null;

                });

                dialog.showAndWait();

            }
        } catch (SQLException e) {
            showError("เกิดข้อผิดพลาด: " + e.getMessage());

        }

    }

    @Override
    public void stop() throws Exception {
        
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }

        super.stop();

    }

}
