package com.qfs.training.rivolition.data.main.serializable;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class SerializableObject<T> implements Serializable{

    private T obj;

    public SerializableObject(T obj){
        this.obj = obj;
    }

   /* public SerializableObject(String path) throws IOException {
        new SerializableObject<T>(readSerializable(path));
    }*/

    public void serializableSaver(String path) {
        File file = new File(path);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(this.obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public T getObj() {
        return this.obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public static Object readSerializable(String path) throws IOException {
        File file = new File(path);
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
