package aron.authent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Account extends DatabaseAPI {


    
    public void addAccount(String email, String password) {
        String hashedPassword = hashPassword(password);
        
        String fields = "email, password_hash";
        String values = "'" + email + "', '" + hashedPassword + "'";
        new DatabaseAPI().insert("users", fields, values);
    }

    public void initAccount() {
         System.out.println("initAccount not implemented");
    }


    public boolean verifyAccount(String userName) {
        System.out.println("verifyAccount not implemented");
        return false;
    }

    public boolean verifyPassword(String userName, String password) {
        System.out.println("verifyPassword not implemented");
        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
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

}

