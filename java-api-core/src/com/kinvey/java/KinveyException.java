/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.java;

/**
 * @author edwardf
 */
public class KinveyException extends RuntimeException{

    private String reason;
    private String fix;
    private String explanation;



    public KinveyException(String reason, String fix, String explanation){
        super(formatMessage(reason, fix, explanation));
        this.reason = reason;
        this.fix = fix;
        this.explanation = explanation;
    }
    
    public KinveyException(String reason){
    	super(formatMessage(reason));
    	this.reason = reason;
    	this.fix = "";
    	this.explanation = "";
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFix() {
        return fix;
    }

    public void setFix(String fix) {
        this.fix = fix;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    private static String formatMessage(String reason, String fix, String explanation){
        return "\nREASON: " + reason + "\n" + "FIX: " + fix + "\n" + "EXPLANATION: " + explanation + "\n";
    }
    
    private static String formatMessage(String reason){
    	return "\nREASON: " + reason;
    		
    }

}
