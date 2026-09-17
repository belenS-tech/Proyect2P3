package service;


import model.FilaActividad;
import model.EstadoReserva;
import model.Reserva;
import model.ResultadoProgramacionActividades;
import repository.ReservaRepository;
import util.DateUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Construye la matriz de programación semanal: filas = horas del
 * día, columnas = días de la semana de referencia, celdas =
 * actividades programadas (puede haber varias por celda).
*/

public class ProgramacionActividadesService {

    private static final LocalTime HORA_INICIO_DIA = LocalTime.of(6, 0);
    private static final LocalTime HORA_FIN_DIA = LocalTime.of(22, 0);

    private final ReservaRepository reservaRepo;

    public ProgramacionActividadesService(ReservaRepository reservaRepo) {
        this.reservaRepo = reservaRepo;
    }

    public ResultadoProgramacionActividades construirMatrizSemana(LocalDate fechaReferencia) {
        if (fechaReferencia == null) {
            throw new IllegalArgumentException("Debe seleccionar una fecha de referencia.");
        }

        LocalDate inicio = DateUtils.inicioSemana(fechaReferencia);
        LocalDate fin = DateUtils.finSemana(fechaReferencia);

        List<LocalDate> dias = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            dias.add(d);
        }

        List<Reserva> reservasSemana = reservaRepo.listar().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .filter(r -> !r.getFecha().isBefore(inicio) && !r.getFecha().isAfter(fin))
                .collect(Collectors.toList());

        List<FilaActividad> filas = new ArrayList<>();
        for (LocalTime hora = HORA_INICIO_DIA; hora.isBefore(HORA_FIN_DIA); hora = hora.plusHours(1)) {
            FilaActividad fila = new FilaActividad(hora.toString());
            for (LocalDate dia : dias) {
                fila.setValor(dia, buscarActividad(reservasSemana, dia, hora));
            }
            filas.add(fila);
        }

        return new ResultadoProgramacionActividades(dias, filas);
    }

    private String buscarActividad(List<Reserva> reservasSemana, LocalDate dia, LocalTime hora) {
        List<String> actividadesEnCelda = new ArrayList<>();

        for (Reserva reserva : reservasSemana) {
            if (!reserva.getFecha().equals(dia)) continue;

            boolean estaEnRango = !hora.isBefore(reserva.getHoraInicio()) && hora.isBefore(reserva.getHoraFin());
            if (!estaEnRango) continue;

            actividadesEnCelda.add(reserva.getActividad() + " (" + reserva.getFuncionarioId() + ")");
        }

        return String.join(" | ", actividadesEnCelda);
    }
}

