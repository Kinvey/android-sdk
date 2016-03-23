/*
 * Copyright (c) 2014, Kinvey, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java;

import com.kinvey.java.query.KinveyClientErrorCode;

/**
 * @author edwardf
 */
public class KinveyException extends RuntimeException{

    private String reason;
    private String fix;
    private String explanation;
    private KinveyClientErrorCode errorCode;



    public KinveyException(KinveyClientErrorCode errorCode){
        super(formatMessage(errorCode.getReason(), errorCode.getExplain(), errorCode.getFix()));

        this.errorCode = errorCode;
        this.reason = errorCode.getReason();
        this.explanation = errorCode.getExplain();
        this.fix = errorCode.getFix();
    }

    public KinveyException(KinveyClientErrorCode errorCode, Exception cause){
        super(formatMessage(errorCode.getReason(), errorCode.getExplain(), cause.toString()));
        this.errorCode = errorCode;
        this.reason = errorCode.getReason();
        this.explanation = errorCode.getExplain();
        this.fix = errorCode.getFix();

    }

    @Deprecated
    public KinveyException(String reason, String fix, String explanation){
        super(formatMessage(reason, fix, explanation));
        this.reason = reason;
        this.fix = fix;
        this.explanation = explanation;
    }
    @Deprecated
    public KinveyException(String reason){
    	super(formatMessage(reason));
    	this.reason = reason;
    	this.fix = "";
    	this.explanation = "";
    }

    public KinveyException(KinveyClientErrorCode errorCode, String reason){
        super(formatMessage(reason));
        this.errorCode = errorCode;
        this.reason = reason;
        this.errorCode = errorCode;
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

    public KinveyClientErrorCode getErrorCode() {
        return errorCode;
    }
}
