package unit;

import org.junit.jupiter.api.Test;
import service.ReservaService;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaServiceTest {
    @Test
    void buscarReservaPorIdNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService recursoService = new ReservaService();
            recursoService.buscarPorId(null);
        });
    }

    @Test
    void registrarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null);
            reservaService.registrar(null);
        });
    }

    @Test
    void actualizarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null);
            reservaService.actualizar(null);
        });
    }

    @Test
    void eliminarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null);
            reservaService.eliminar(null);
        });
    }
}
