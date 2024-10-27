package by.prokopovich.time_tracker.utils;

import java.time.format.DateTimeFormatter;

/**
 * Класс для хранения разных констант и прочего
 */
public class AuxiliaryElements {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private AuxiliaryElements() {
        throw new UnsupportedOperationException("Класс утилитарный не предназначен для инстанцирования");
    }
}
