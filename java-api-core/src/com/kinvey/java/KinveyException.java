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

}
