module aron.authent {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires javafx.graphics;
    requires authutils;

    opens aron.authent to javafx.fxml;
    exports aron.authent;
}
