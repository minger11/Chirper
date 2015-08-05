package model;

import java.util.*;
/**
 * Created by mingerso on 31/07/15.
 */
public class Session {
        String id;
        String ipAddress;
        long since;

        //creates a new session given an ip address
        //creates a random session id and stores the time and ip address
        public Session(String ipAddress){
        this.id = java.util.UUID.randomUUID().toString();
        this.since = System.currentTimeMillis();
        this.ipAddress = ipAddress;
        }

        //returns the ip address
        public String getIpAddress() {
        return this.ipAddress;
        }

        //returns the time session started in millis
        public long getSince() {
        return this.since;
        }
}
