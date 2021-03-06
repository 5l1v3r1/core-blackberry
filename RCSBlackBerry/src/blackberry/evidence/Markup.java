//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Markup.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.evidence;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.crypto.EncryptionPKCS5;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.fs.AutoFile;
import blackberry.fs.Path;
import blackberry.module.BaseModule;
import blackberry.utils.Utils;

/**
 * The Class Markup.
 */
public class Markup {

    public static final String MARKUP_EXTENSION = ".qmm";
    public static byte markupSeed;
    public static boolean markupInit;

    private String markupId = "core";

    //#ifdef DEBUG
    private static Debug debug = new Debug("Markup", DebugLevel.VERBOSE);
    //#endif

    EncryptionPKCS5 encryption;
    EvidenceCollector logCollector;

    private Markup() {
        encryption = new EncryptionPKCS5(Encryption.getKeys().getLogKey());
        logCollector = EvidenceCollector.getInstance();
    }

    protected Markup(final String agentId_) {
        this();
        markupId = agentId_;
    }

    public Markup(final int id) {
        this();
        markupId = Integer.toString(id);
    }

    protected Markup(final String string, int num) {
        this();
        markupId = string + num;

    }

    public Markup(Event event) {
        this("EVT" + event.getType(), event.getEventId());
    }

    public Markup(BaseModule module) {
        this("MOD" + module.getType());
    }

    public Markup(Event module, int id) {
        this("EVT" + module.getType(), id);
    }
    
    public Markup(BaseModule module, int id) {
        this("MOD" + module.getType(), id);
    }

    /**
     * Crea un markup vuoto.
     * 
     * @return true if successful
     */
    public boolean createEmptyMarkup() {
        return writeMarkup(null);
    }

    /**
     * Override della funzione precedente: invece di generare il nome da una
     * stringa lo genera da un numero. Se la chiamata fallisce la funzione torna
     * una stringa vuota.
     * 
     * @param agentId
     *            the agent id
     * @param addPath
     *            the add path
     * @return the string
     */
    static String makeMarkupName(final int agentId, final boolean addPath) {
        //#ifdef DBC
        Check.requires(agentId >= 0, "agentId < 0");
        //#endif
        final String logName = NumberUtilities.toString(agentId, 16, 4);
        //#ifdef DEBUG
        debug.trace("makeMarkupName from: " + logName);
        //#endif

        final String markupName = makeMarkupName(logName, addPath);
        return markupName;
    }

    static String makeMarkupName(String agentId, final boolean addPath) {
        // final String markupName = Integer.toHexString(agentId);
        final String markupName = Utils.byteArrayToHex(Encryption.SHA1(agentId
                .getBytes()));
        //#ifdef DBC
        Check.requires(markupName != null, "null markupName");
        Check.requires(markupName.length() > 0, "empty markupName");
        //#endif

        String encName = ""; //$NON-NLS-1$

        if (addPath) {
            encName = Path.markup();
        }

        encName += Encryption.encryptName(markupName + MARKUP_EXTENSION,
                getMarkupSeed());

        //#ifdef DBC
        Check.asserts(markupInit, "makeMarkupName: " + markupInit); //$NON-NLS-1$
        //#endif
        return encName;
    }

    private static int getMarkupSeed() {
        if (!markupInit) {
            final Keys keys = Encryption.getKeys();
            final byte[] challengeKey = keys.getProtoKey();
            //#ifdef DBC
            Check.asserts(challengeKey != null,
                    "makeMarkupName: challengeKey==null");
            //#endif
            markupSeed = challengeKey[0];
            markupInit = true;
        }

        return markupSeed;
    }

    public static synchronized int removeMarkups() {

        //#ifdef DEBUG
        debug.trace("removeMarkups");
        //#endif

        int numDeleted = 0;

        AutoFile dir = new AutoFile(Path.markup());
        Enumeration list = dir.list();
        for (Enumeration iterator = list; iterator.hasMoreElements();) {
            String filename = (String) iterator.nextElement();
            AutoFile file = new AutoFile(Path.markup(), filename);
            file.delete();
            numDeleted++;
        }

        return numDeleted;
    }

    /**
     * Checks if is markup.
     * 
     * @return true, if is markup
     */
    public synchronized boolean isMarkup() {
        //#ifdef DBC
        Check.requires(markupId != null, "markupId null");
        //#endif

        final String markupName = makeMarkupName(markupId, true);
        //#ifdef DBC
        Check.asserts(markupName != "", "markupName empty");
        //#endif

        final AutoFile fileRet = new AutoFile(markupName, true);
        return fileRet.exists();
    }

    /**
     * Legge il file di markup specificato dall'AgentId (l'ID dell'agente che
     * l'ha generato), torna un array di dati decifrati. Se il file non viene
     * trovato o non e' possibile decifrarlo correttamente, torna null. Se il
     * Markup e' vuoto restituisce un byte[0]. E' possibile creare dei markup
     * vuoti, in questo caso non va usata la ReadMarkup() ma semplicemente la
     * IsMarkup() per vedere se e' presente o meno.
     * 
     * @return the byte[]
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public synchronized byte[] readMarkup() throws IOException {
        //#ifdef DBC
        Check.requires(markupId != null, "markupId null");
        //#endif

        final String markupName = makeMarkupName(markupId, true);
        //#ifdef DBC
        Check.asserts(markupName != "", "markupName empty");
        //#endif

        final AutoFile fileRet = new AutoFile(markupName, true);

        if (fileRet.exists()) {
            final byte[] encData = fileRet.read();
            //final int len = Utils.byteArrayToInt(encData, 0);

            byte[] plain = null;
            try {
                plain = encryption.decryptDataRim(encData, encData.length, 0);
            } catch (CryptoException e) {
                return null;
            }
            //#ifdef DBC
            Check.asserts(plain != null, "wrong decryption: null");
            //#endif

            return plain;
        } else {
            //#ifdef DEBUG
            debug.trace("Markup file does not exists");
            //#endif
            return null;
        }
    }

    /**
     * Removes the markup.
     */
    public synchronized void removeMarkup() {

        //#ifdef DEBUG
        debug.trace("removeMarkup: ");
        //#endif
        //#ifdef DBC
        Check.requires(markupId != null, "markupId null");
        //#endif

        final String markupName = makeMarkupName(markupId, true);
        //#ifdef DBC
        Check.asserts(markupName != "", "markupName empty");
        //#endif

        //#ifdef DEBUG
        debug.trace("removeMarkup: " + markupName);
        //#endif
        final AutoFile remove = new AutoFile(markupName, true);
        remove.delete();
    }

    /**
     * Scrive un file di markup per salvare lo stato dell'agente, il parametro
     * e' il buffer di dati. Al termine della scrittura il file viene chiuso,
     * non e' possibile fare alcuna Append e un'ulteriore chiamata alla
     * WriteMarkup() comportera' la sovrascrittura del vecchio markup. La
     * funzione torna TRUE se e' andata a buon fine, FALSE altrimenti. Il
     * contenuto scritto e' cifrato.
     * 
     * @param data
     *            the data
     * @return true, if successful
     */
    public synchronized boolean writeMarkup(final byte[] data) {
        final String markupName = makeMarkupName(markupId, true);
        //#ifdef DBC
        Check.asserts(markupName != "", "markupName empty");
        //#endif

        final AutoFile fileRet = new AutoFile(markupName, true);

        // se il file esiste viene azzerato
        fileRet.create();

        if (data != null) {
            byte[] encData;
            try {
                encData = encryption.encryptDataRim(data, 0);
                //#ifdef DBC
                Check.asserts(encData.length >= data.length, "strange data len");
                //#endif
                //fileRet.write(data.length);
                fileRet.append(encData);
            } catch (CryptoTokenException e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("writeMarkup");
                //#endif
                return false;
            } catch (CryptoUnsupportedOperationException e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("writeMarkup");
                //#endif
                return false;
            } catch (IOException e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("writeMarkup");
                //#endif
                return false;
            }

        }

        return true;
    }

    public Date readDate() {
        byte[] data;
        Date date = null;
        try {
            data = readMarkup();

            DataBuffer buffer = new DataBuffer(data, 0, data.length, true);
            long time = buffer.readLong();
            date = new Date(time);
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("readDate");
            //#endif
        }
        return date;
    }

    public void write(Date date) {
        DataBuffer buffer = new DataBuffer();
        buffer.writeLong(date.getTime());
        writeMarkup(buffer.getArray());

    }

    public String[] readMarkupStringArray() {
        byte[] data;
        Date date = null;
        String[] array= null;
        try {
            data = readMarkup();

            DataBuffer buffer = new DataBuffer(data, 0, data.length, true);
            int size = buffer.readInt();
            array = new String[size];
            
            for(int i = 0; i<size; i++){
                String value = buffer.readUTF();
                array[i]=value;
            }
            
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("readMarkupStringArray");
            //#endif
        }
        return array;
    }
    
    public void writeMarkupStringArray(String[] values) throws IOException {
        DataBuffer buffer = new DataBuffer();
        buffer.writeInt(values.length);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            buffer.writeUTF(value);
        }
        writeMarkup(buffer.getArray());
    }

}
