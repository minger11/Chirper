package model;

import org.mindrot.jbcrypt.BCrypt;

/**
 * A little demo code to show how BCrypt is used to hash passwords
 */
public class BCrypter {

    // Eek, not threadsafe.
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
