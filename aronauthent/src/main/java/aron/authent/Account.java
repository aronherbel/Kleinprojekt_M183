package aron.authent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
//import de.taimos.totp.TOTP;


public class Account extends DatabaseAPI {

    DatabaseAPI db = new DatabaseAPI();
    
    public void addAccount(String email, String password) {

        

        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        String saltFields = "salt_value";
        String saltValues = "'" + salt + "'"; 
        int saltId = db.insertAndGetId("salts", saltFields, saltValues);
        
        String fields = "email, password_hash, salt_id";
        String values = "'" + email + "', '" + hashedPassword + "'," + saltId;

        db.insert("users", fields, values);
    }

    public void initAccount() {
        // Create "users" table
        String usersFields = 
            "\"user_id\" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "\"email\" TEXT UNIQUE, " +
            "\"password_hash\" TEXT, " +
            "\"salt_id\" INTEGER, " +
            "\"2fa_enabled\" INTEGER DEFAULT 0, " +
            "FOREIGN KEY(\"salt_id\") REFERENCES \"salts\"(\"salt_id\")";
        
        db.createTable("users", usersFields);

        // Create "salts" table
        String saltsFields = 
            "\"salt_id\" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "\"salt_value\" TEXT";
        
        db.createTable("salts", saltsFields);
    }


    public boolean verifyAccount(String email) {
        System.out.println("verifyAccount");
        return db.isKeyAvailable("users", "email", "'" + email + "'");
    }

    public boolean verifyPassword(String email, String password) {
        System.out.println("verifyPassword");
        String storedPasswordHash = db.getValue("users", "email", "'" + email + "'", "password_hash");
        String storedSaltIdString = db.getValue("users", "email", "'" + email + "'", "salt_id");
        int storedSaltId = Integer.parseInt(storedSaltIdString);

        String storedSalt = db.getValue("salts", "salt_id",  "'" + storedSaltId + "'", "salt_value" );
        

        if (storedPasswordHash == null || storedSalt == null) {
            System.out.println("Kein benutzer oder salt gefunden");
            return false; // Benutzer oder Salt nicht gefunden
        }

        String hashedInputPassword = hashPassword(password, storedSalt);

        return storedPasswordHash.equals(hashedInputPassword);
    }

    private String hashPassword(String password, String salt) {
        try {
           
            String passwordWithSalt = password + salt;
            
            // Berechne den SHA-256 Hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(passwordWithSalt.getBytes());
    
            // Konvertiere den Hash in einen hexadezimalen String
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

}
/* 
    public static String getGoogleAuthenticatorBarCode(String secretKey, String account) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void createQRCode(String barCodeData, String filePath, int height, int width)
            throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE,
                width, height);
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);
        }
    }
}

     public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return hexKey; //TOTP.getOTP(hexKey);
    }



    

    public static void infinityGeneratingCodes(String secretKey) {
        String lastCode = null;
        while (true) {
            String code = "code";//getTOTPCode(secretKey);
            if (!code.equals(lastCode)) {
                System.out.println(code);
            }
            lastCode = code;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {};
        }
    }

    public boolean verify2FACode(String secret, int code) {
        // Verwende eine Bibliothek wie Google Authenticator, um den Code zu überprüfen
       // GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return true; // gAuth.getTotpPassword(secret) == code;
    }


}

*/