package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;
import com.kinvey.android.model.User;

public class TestUser extends User {

    @Key("companyName")
    private String companyName;

    @Key
    private InternalUserEntity internalUserEntity;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public InternalUserEntity getInternalUserEntity() {
        return internalUserEntity;
    }

    public void setInternalUserEntity(InternalUserEntity internalUserEntity) {
        this.internalUserEntity = internalUserEntity;
    }
}
