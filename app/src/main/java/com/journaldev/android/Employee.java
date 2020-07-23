package com.journaldev.android;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class Employee extends RealmObject {


@Required
public String name;
@Required
public String mytitle;

    @Required
   public String _partition = "My Project";

    @Required
    @PrimaryKey
    ObjectId _id;

    public Employee() {
        _id = new ObjectId();
    }
}

