package service;

import model.*;
import repository.RecursoRepository;
import repository.ReservaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Construye la matriz de calendarización
*/
public class CalendarizacionRecursosService {

    private static final LocalTime HORA_INICIO_DIA = LocalTime.of(6, 0);
    private static final LocalTime HORA_FIN_DIA = LocalTime.of(22, 0);

    private final RecursoRepository recursoRepo;
    private final ReservaRepository reservaRepo;

    public CalendarizacionRecursosService(RecursoRepository recursoRepo, ReservaRepository reservaRepo) {
        this.recursoRepo = recursoRepo;
        this.reservaRepo = reservaRepo;
    }

    public ResultadoCalendarizacion construirMatriz(LocalDate fecha, String categoriaId) {
        if (fecha == null) {
            throw new IllegalArgumentException("Debe seleccionar una fecha.");
        }
        if (categoriaId == null || categoriaId.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }

        List<Recurso> recursos = recursoRepo.buscarPorCategoria(categoriaId);
        if (recursos.isEmpty()) {
            return new ResultadoCalendarizacion(recursos, new ArrayList<>());
        }

        List<Reserva> reservasDelDia = reservaRepo.listar().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .filter(r -> fecha.equals(r.getFecha()))
                .collect(Collectors.toList());

        List<FilaCalendarizacion> filas = new ArrayList<>();
        for (LocalTime hora = HORA_INICIO_DIA; hora.isBefore(HORA_FIN_DIA); hora = hora.plusHours(1)) {
            FilaCalendarizacion fila = new FilaCalendarizacion(hora.toString());
            for (Recurso recurso : recursos) {
                fila.setValor(recurso.getId(), buscarOcupacion(reservasDelDia, hora, categoriaId, recurso.getId()));
            }
            filas.add(fila);
        }

        return new ResultadoCalendarizacion(recursos, filas);
    }

    private String buscarOcupacion(List<Reserva> reservasDelDia, LocalTime hora,
                                   String categoriaId, String recursoId) {
        for (Reserva reserva : reservasDelDia) {
            boolean estaEnRango = !hora.isBefore(reserva.getHoraInicio()) && hora.isBefore(reserva.getHoraFin());
            if (!estaEnRango) continue;

            for (DetalleReserva detalle : reserva.getDetalles()) {
                boolean mismaCategoria = detalle.getCategoria() != null
                        && categoriaId.equals(detalle.getCategoria().getId());
                boolean mismoRecurso = detalle.getRecurso() != null
                        && recursoId.equals(detalle.getRecurso().getId());
                if (mismaCategoria && mismoRecurso) {
                    return reserva.getActividad() + " - " + reserva.getFuncionarioId();
                }
            }
        }
        return "";
    }
}
