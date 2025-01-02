module com.example.uas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;

    opens com.example.uas to javafx.fxml;
    exports com.example.uas;
}
