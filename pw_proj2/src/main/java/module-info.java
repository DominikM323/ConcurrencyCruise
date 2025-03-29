module com.example.pw_proj2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires java.desktop;


    opens com.example.pw_proj2 to javafx.fxml;
    exports com.example.pw_proj2;
}