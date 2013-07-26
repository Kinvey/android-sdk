/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.ticketview.model;

import android.text.format.DateFormat;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class TicketEntity extends GenericJson {
    @Key("_id")
    private String ticketId;
    @Key
    private String requestedBy;
    @Key
    private String description;
    @Key
    private String requestDate;
    @Key
    private String subject;
    @Key
    private String status;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestDate() {
        return requestDate.toString();
    }

    public void setRequestDate(String requestDate) {
        this.requestDate=requestDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
