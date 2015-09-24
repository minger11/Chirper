package model;

import java.util.*;
/**
 * Created by mingerso on 31/07/15.
 */
public class Session {
        String id;
        String ipAddress;
        long since;

        //overload for now
        public Session(String id, String ipAddress, long since) {
                this.id = id;
                this.ipAddress = ipAddress;
                this.since = since;
        }

        public String getId() {
                return id;
        }

        //returns the ip address
        public String getIpAddress() {
        return ipAddress;
        }

        //returns the time session started in millis
        public long getSince() {
        return since;
        }
}

