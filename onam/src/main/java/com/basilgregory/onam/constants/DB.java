package com.basilgregory.onam.constants;

import android.util.Base64;

import com.basilgregory.onam.builder.DbMetaData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by donpeter on 8/29/17.
 */

public class DB {
    public static String PRIMARY_KEY_ID = "_id";

    static String toString(Serializable serializable) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( serializable );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);
    }

    static DbMetaData toObject(String serializedString) throws Exception{
        if (serializedString == null) return new DbMetaData();
        byte [] data = Base64.decode( serializedString ,Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object object  = ois.readObject();
        ois.close();
        return (DbMetaData) object;
    }
}
