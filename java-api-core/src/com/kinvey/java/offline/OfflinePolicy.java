/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.offline;


import java.io.IOException;

/**
 * @author edwardf
 */
public enum OfflinePolicy {

    ALWAYSONLINE{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            return offlineRequest.offlineFromService(true);
        }
    },


    SYNC_ANYTIME{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            return offlineRequest.offlineFromService(true);
        }
    },

    SYNC_FOREGROUND{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            return offlineRequest.offlineFromService(true);
        }
    },

    SYNC_BACKGROUND{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            return offlineRequest.offlineFromService(true);
        }
    };









    public abstract <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException;



    }
