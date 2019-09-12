package com.kinvey.java.store

/**
 * Created by yuliya on 2/20/17.
 */
class KinveyLiveServiceStatus(var liveServiceStatusType: StatusType?, messages: Array<String>) {

    var status: Int = 0

    var message: String? = null

    var timeStamp: String? = null

    var channel: String? = null

    var channelGroup: String? = null


    init {

        when (liveServiceStatusType) {
            KinveyLiveServiceStatus.StatusType.STATUS_CONNECT, KinveyLiveServiceStatus.StatusType.STATUS_DISCONNECT -> {
                status = Integer.parseInt(messages[0])
                message = messages[1]
                channelGroup = messages[2]
            }
            KinveyLiveServiceStatus.StatusType.STATUS_PUBLISH -> {
                status = Integer.parseInt(messages[0])
                message = messages[1]
                timeStamp = messages[2]
                channelGroup = messages[3]
            }
        }
    }

    /**
     * Enum representing all the types of status messages that can be received for LiveService.
     */
    enum class StatusType {

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
