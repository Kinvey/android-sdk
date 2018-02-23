package com.kinvey.java.store;

/**
 * Created by yuliya on 2/20/17.
 */
public class KinveyLiveServiceStatus {

    private StatusType liveServiceStatusType;

    private int status;

    private String message;

    private String timeStamp;

    private String channel;

    private String channelGroup;


    public KinveyLiveServiceStatus(StatusType type, String[] messages) {
        this.liveServiceStatusType = type;

        switch (liveServiceStatusType) {
            case STATUS_CONNECT:
            case STATUS_DISCONNECT:
                status = Integer.parseInt((messages[0]));
                message = messages[1];
                channelGroup = messages[2];
                break;
            case STATUS_PUBLISH:
                status = Integer.parseInt((messages[0]));
                message = messages[1];
                timeStamp = messages[2];
                channelGroup = messages[3];
                break;
        }
    }

    public StatusType getLiveServiceStatusType() {
        return liveServiceStatusType;
    }

    public void setLiveServiceStatusType(StatusType liveServiceStatusType) {
        this.liveServiceStatusType = liveServiceStatusType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelGroup() {
        return channelGroup;
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }

    /**
     * Enum representing all the types of status messages that can be received for LiveService.
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
