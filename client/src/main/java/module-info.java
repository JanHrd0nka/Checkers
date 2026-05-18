module cz.vse.java.checkers.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires cz.vse.java.checkers.common;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    opens cz.vse.java.checkers.client to javafx.fxml;
    opens cz.vse.java.checkers.client.Game to javafx.fxml;
    exports cz.vse.java.checkers.client to javafx.graphics;
    exports cz.vse.java.checkers.client.Networking to javafx.graphics;
    exports cz.vse.java.checkers.client.Game to javafx.fxml, javafx.graphics;
    opens cz.vse.java.checkers.client.Networking to javafx.fxml;
}