package util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;


public class DateUtils {

    public static LocalDate inicioSemana(LocalDate fechaReferencia) {
        return fechaReferencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate finSemana(LocalDate fechaReferencia) {
        return inicioSemana(fechaReferencia).plusDays(6);
    }

    public static String etiquetaSemana(LocalDate fechaReferencia) {
        LocalDate inicio = inicioSemana(fechaReferencia);
        LocalDate fin = finSemana(fechaReferencia);
        return "Semana del " + inicio + " al " + fin;
    }
}