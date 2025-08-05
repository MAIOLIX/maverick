package com.maiolix.maverick.constants;

/**
 * Costanti utilizzate nel MaverickController e servizi correlati
 */
public final class MaverickConstants {

    private MaverickConstants() {
        // Prevent instantiation
    }

    // === RESPONSE KEYS ===
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String TIMESTAMP = "timestamp";
    public static final String MODEL_NAME = "modelName";
    public static final String VERSION = "version";
    public static final String FILE_SIZE = "fileSize";
    public static final String IS_ACTIVE = "isActive";
    public static final String STATISTICS = "statistics";

    // === STATUS VALUES ===
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";

    // === MESSAGE FRAGMENTS ===
    public static final String MODELLO = "Modello ";
    public static final String VERSIONE = " versione ";
    public static final String NON_TROVATO_DB = " non trovato nel database";
}
