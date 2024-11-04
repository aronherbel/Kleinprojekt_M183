package aron.authent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import twofactor.Utils;


public class AccountController {

    private Account account;
    
    @FXML
    private Button btLogin;
    
    @FXML
    private Button btLogout;

    @FXML
    private Button btSignUp;

    @FXML
    private Label lbLoginMessage;

    @FXML
    private Label lbSignUpMessage;

    @FXML
    private PasswordField pfLoginPassword;

    @FXML
    private PasswordField pfSignUpConfirmPassword;

    @FXML
    private PasswordField pfSignUpPassword;
     
    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSignUpEmail;

    @FXML
    private TextField tfUsername;

    
    @FXML
    private ImageView ivQRCode;

    @FXML
    private Label lb2FAMessage;

    @FXML
    private TextField tf2FACode;

    private String secretKey;




    
    @FXML
    private void initialize() throws Exception {
        // create and init DB-Tables
        account = new Account();
        account.initAccount();
    }   

    @FXML
    private void onSignUp(ActionEvent event) throws Exception  {    

        String name = tfSignUpEmail.getText();
        if (name.isEmpty()) {
            lbSignUpMessage.setText("Type in email");
            return;
        }

        String pw = pfSignUpPassword.getText().trim();
        if (pw.equals("")) {
            lbSignUpMessage.setText("Enter a plausible password");
            return;
        }

        if (!pw.equals(pfSignUpConfirmPassword.getText())) {
            lbSignUpMessage.setText("Password and confirmed password are not identical");
            return;
        }

        if (!isPasswordStrong(pw)) {
            lbSignUpMessage.setText("Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.");
            return;
        }

        // verify account 
        if (account.verifyAccount(name)) {
            lbSignUpMessage.setText("Email " + name + " has already an account");
            return;
        }

        
        // add new account
        account.addAccount(name, pw);
        
        // select tab 'Log In'
        tabPane.getTabs().get(0).setDisable(true);
        
        // reset login and signup
        resetLogin();
        resetSignup();
        
        // select tab 'Log in'
        tabPane.getSelectionModel().select(1);
    }

    @FXML
    private void onLogin(ActionEvent event) throws Exception {
        String name = tfUsername.getText();
        String pw = pfLoginPassword.getText();
                        
        if (account.verifyPassword(name, pw)) {
            
            secretKey = Utils.generateSecretKey();
            String barCodeUrl = Utils.getGoogleAuthenticatorBarCode(secretKey, name, name + "AG");
            System.out.println(barCodeUrl);

            try {
                String qrCodePath = "path_to_save_qr_code.png"; // Pfad, wo der QR-Code gespeichert werden soll
                Utils.createQRCode(barCodeUrl, qrCodePath, 200, 200); // QR-Code erstellen
                Image qrImage = new Image(new File(qrCodePath).toURI().toString()); // Bild laden
                ivQRCode.setImage(qrImage); // Bild im ImageView anzeigen
                
            } catch (WriterException | IOException e) {
                e.printStackTrace();
            }

            tabPane.getTabs().get(0).setDisable(false);
            tabPane.getTabs().get(1).setDisable(false);
            tabPane.getTabs().get(2).setDisable(false);
            resetLogin();
            tabPane.getSelectionModel().select(2);
        } else {
            lbLoginMessage.setText("'Email' or 'Password' are wrong");
            tabPane.getTabs().get(0).setDisable(false);
        }
    }
   
    @FXML
    private void onLogout(ActionEvent event) {
        // set tabs
        tabPane.getTabs().get(0).setDisable(false);
        tabPane.getTabs().get(1).setDisable(false);
        tabPane.getTabs().get(2).setDisable(false);
        tabPane.getTabs().get(3).setDisable(true);
        
        // reset login and select tab 'Log in'
        resetLogin();   
        tabPane.getSelectionModel().select(1);      
    }

    @FXML
    public void onVerify2FA(ActionEvent event) {
        String code = tf2FACode.getText();
        
        // Überprüfen des eingegebenen Codes
        if (isValidCode(code)) {
            // Erfolgreiche Verifizierung, weiter zur nächsten Ansicht
            tabPane.getTabs().get(0).setDisable(true);
            tabPane.getTabs().get(1).setDisable(true);
            tabPane.getTabs().get(2).setDisable(true);
            tabPane.getTabs().get(3).setDisable(false);
            lb2FAMessage.setText("Code erfolgreich verifiziert!");
            tabPane.getSelectionModel().select(3); // Wechselt zur nächsten Tab
        } else {
            // Fehlerhafte Eingabe
            lb2FAMessage.setText("Ungültiger Code. Bitte erneut eingeben.");
            tf2FACode.clear(); // Eingabefeld zurücksetzen
        }
    }


    private boolean isValidCode(String code) {
        try {
            // Erzeuge den erwarteten TOTP-Code anhand des gespeicherten Secret Keys
            String expectedCode = Utils.getTOTPCode(secretKey);
    
            // Vergleiche den eingegebenen Code mit dem erwarteten Code
            return code.equals(expectedCode);
        } catch (NumberFormatException e) {
            // Der Code ist nicht numerisch, daher ungültig
            return false;
        } catch (Exception e) {
            // Fehler bei der Code-Generierung, Rückgabewert ungültig
            e.printStackTrace();
            return false;
        }
    }

    
    private void resetLogin() {
        tfUsername.setText("");
        pfLoginPassword.setText("");
        lbLoginMessage.setText("Login with your account");
    } 

    private void resetSignup() {
        tfSignUpEmail.setText("");
        pfSignUpPassword.setText("");
        pfSignUpConfirmPassword.setText("");
        lbSignUpMessage.setText("Create Account");
    }

    private boolean isPasswordStrong(String password) {
        // Überprüfen der Länge
        if (password.length() < 8) {
            return false;
        }
    
        // Überprüfen auf Großbuchstaben, Kleinbuchstaben, Zahlen und Sonderzeichen
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
    
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
    
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}
