module com.example.proprojectfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;
    opens com.example.proprojectfx.controller to javafx.fxml, javafx.base;  // <-- Ajoutez javafx.base ici
    opens com.example.proprojectfx to javafx.fxml;
    exports com.example.proprojectfx;
}