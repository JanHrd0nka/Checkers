module cz.vse.java.checkers.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires cz.vse.java.checkers.common;
    requires java.logging;

    opens cz.vse.java.checkers.client to javafx.fxml;
    exports cz.vse.java.checkers.client to javafx.graphics;
}