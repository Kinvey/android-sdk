package com.kinvey.java.store;

/**
 * Created by yuliya on 2/20/17.
 */
class KinveyRealtimeStatus {

    private StatusType realtimeStatusType;

    private int status;

    private String message;

    private String timeStamp;

    private String channel;

    private String channelGroup;


    public KinveyRealtimeStatus(StatusType type, String[] messagess) {
        this.realtimeStatusType = type;

        switch (realtimeStatusType) {
            case STATUS_CONNECT:
            case STATUS_DISCONNECT:
                status = Integer.parseInt((messagess[0]));
                message = messagess[1];
                channelGroup = messagess[2];
                break;
            case STATUS_PUBLISH:
                status = Integer.parseInt((messagess[0]));
                message = messagess[1];
                timeStamp = messagess[2];
                channelGroup = messagess[3];
                break;
        }
    }

    public void setRealtimeStatusType(StatusType realtimeStatusType) {
        this.realtimeStatusType = realtimeStatusType;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }

    /**
     * Enum representing all the types of status messages that can be received for realtime.
     */
    public enum StatusType {

        /**
         * connection status message
         */
        STATUS_CONNECT,

        /**
         * disconnection status message
         */
        STATUS_DISCONNECT,

        /**
         * publish status message
         */
        STATUS_PUBLISH
    }

}
