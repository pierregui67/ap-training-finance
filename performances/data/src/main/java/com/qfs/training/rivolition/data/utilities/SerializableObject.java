package com.qfs.training.rivolition.data.utilities;

import java.io.*;

/**
 * This class allows to save important informations through serializable objects.
 * @param <T> : the data to be saved.
 */
public class SerializableObject<T> implements Serializable{

    private T obj;

    public SerializableObject(T obj){
        this.obj = obj;
    }

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
