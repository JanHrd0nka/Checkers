module cz.vse.java.checkers.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens cz.vse.java.checkers.client to javafx.fxml;
    exports cz.vse.java.checkers.client to javafx.graphics;
}