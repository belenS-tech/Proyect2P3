package service;

import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import repository.RecursoXmlRepository;
import repository.ReservaRepository;

import java.util.List;
import java.util.Objects;

public class ReservaService {

    private final DisponibilidadService disponibilidadService;
    private final ReservaRepository reservaRepository;

    public ReservaService(){
        this.reservaRepository = null;
        this.disponibilidadService = null;
    }

    public ReservaService(DisponibilidadService disponibilidadService, ReservaRepository reservaRepository) {
        this.disponibilidadService = disponibilidadService;
        this.reservaRepository = reservaRepository;
    }

    public ReservaService(ReservaRepository reservaRepository) {
        this(new DisponibilidadService(new RecursoXmlRepository(), reservaRepository), reservaRepository);
    }

    public List<Reserva> listarTodos() {
        return reservaRepository.listar();
    }

    public Reserva buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser nulo o vacío");
        }
        return reservaRepository.buscarPorId(id.trim());
    }

    public void registrar(Reserva reserva) {
        validarReservaBase(reserva);

        if (reserva.getDetalles() != null && !reserva.getDetalles().isEmpty()) {
            for (DetalleReserva detalle : reserva.getDetalles()) {
                Recurso recursoDisponible = disponibilidadService.buscarRecursoDisponible(
                        detalle.getCategoria(),
                        reserva.getFecha(),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin()
                );

                if (recursoDisponible == null || !Objects.equals(recursoDisponible.getId(), detalle.getRecurso().getId())) {
                    throw new IllegalArgumentException("El recurso no está disponible");
                }
            }
        }
        reservaRepository.guardar(reserva);
    }

    public void actualizar(Reserva reserva) {
        validarReservaBase(reserva);
        if (reservaRepository.buscarPorId(reserva.getId().trim()) == null) {
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
        }
        reservaRepository.actualizar(reserva);
    }

    public void eliminar(Reserva reserva) {
        validarReservaBase(reserva);

        if (reservaRepository.buscarPorId(reserva.getId().trim()) == null) {
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
        }
        reservaRepository.eliminar(reserva);
    }

    private void validarReservaBase(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (reserva.getId() == null || reserva.getId().isBlank()) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser nulo o vacío");
        }
        if (reserva.getActividad() == null || reserva.getActividad().isBlank()) {
            throw new IllegalArgumentException("La actividad no puede ser nula o vacía");
        }
        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (reserva.getHoraInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio no puede ser nula");
        }
        if (reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("La hora de fin no puede ser nula");
        }
        if (reserva.getFuncionarioId() == null || reserva.getFuncionarioId().isBlank()) {
            throw new IllegalArgumentException("El funcionario no puede ser nulo o vacío");
        }
        if (reserva.getEstado() == null) {
            throw new IllegalArgumentException("El estado de la reserva no puede ser nulo");
        }
        if (EstadoReserva.ACTIVA.equals(reserva.getEstado()) && reserva.getHoraInicio().isAfter(reserva.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
        }
    }
}
