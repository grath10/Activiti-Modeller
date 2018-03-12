package com.workflow.entity.activiti;

import java.io.Serializable;

public interface User extends Serializable {

    String getId();

    void setId(String id);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getEmail();

    void setEmail(String email);

    String getPassword();

    void setPassword(String string);

    boolean isPictureSet();
}