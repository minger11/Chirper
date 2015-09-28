package model;

import org.mindrot.jbcrypt.BCrypt;


public class BCrypter {


    static String hashed;

    //returns an encrypted string from the passed in plaintext string
    public static String encrypt(String s) {
        String hashed = BCrypt.hashpw(s, BCrypt.gensalt());
        return hashed;
    }

    //returns true if passed plaintext string matches passes hashed string
    public static boolean checkpassword(String plaintext, String hashed){
        boolean valid = BCrypt.checkpw(plaintext, hashed);
        return valid;
    }

}
