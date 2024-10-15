module aron.authent {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires java.sql;

    opens aron.authent to javafx.fxml;
    exports aron.authent;
}
