package com.basilgregory.onam.android;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donpeter on 9/1/17.
 */

public class DbMetaData implements Serializable{
    int version = 0;
    List<String> tableNames = new ArrayList<>();
}
