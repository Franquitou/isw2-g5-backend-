package isw2_G5.liga_amateur.partido;

import static org.assertj.core.api.Assertions.assertThat;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.common.error.ConflictoException;
import isw2_G5.liga_amateur.partido.dto.PartidoWriteRequest;
import isw2_G5.liga_amateur.partido.repository.PartidoRepository;
import isw2_G5.liga_amateur.partido.service.PartidoService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Comprueba que solicitudes de programación simultáneas no generen superposiciones (RN-01, RN-02 y
 * un equipo por jornada). Cada solicitud corre en su propio hilo y su propia transacción, que se confirma
 * de verdad: por eso esta clase no es {@code @Transactional} y limpia la base al terminar cada prueba.
 *
 * <p>Usa una H2 en memoria propia con {@code schema.sql} y {@code data.sql}; no toca {@code ./data/liga_amateur}.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:programacion-concurrencia-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class ProgramacionPartidosConcurrenciaTest {

    private static final OffsetDateTime SABADO_18 = OffsetDateTime.parse("2026-11-07T18:00:00-03:00");

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM partido");
        jdbcTemplate.update("DELETE FROM equipo");
    }

    @Test
    void soloUnaDeVariasSolicitudesSimultaneasObtieneLaMismaCancha() throws Exception {
        List<PartidoWriteRequest> solicitudes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int local = crearEquipo("Local " + i);
            int visitante = crearEquipo("Visitante " + i);
            solicitudes.add(new PartidoWriteRequest(1, 1, local, visitante, 1, i + 1, SABADO_18));
        }

        assertThat(ejecutarEnSimultaneo(solicitudes)).isEqualTo(1);
        assertThat(partidoRepository.count()).isEqualTo(1);
    }

    @Test
    void soloUnaDeVariasSolicitudesSimultaneasObtieneElMismoArbitro() throws Exception {
        List<PartidoWriteRequest> solicitudes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int local = crearEquipo("Local " + i);
            int visitante = crearEquipo("Visitante " + i);
            solicitudes.add(new PartidoWriteRequest(1, 1, local, visitante, i + 1, 1, SABADO_18));
        }

        assertThat(ejecutarEnSimultaneo(solicitudes)).isEqualTo(1);
        assertThat(partidoRepository.count()).isEqualTo(1);
    }

    @Test
    void unEquipoNoQuedaProgramadoDosVecesEnLaMismaJornadaPorSolicitudesSimultaneas() throws Exception {
        int norte = crearEquipo("Barrio Norte");
        int sur = crearEquipo("Barrio Sur");
        List<PartidoWriteRequest> solicitudes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            solicitudes.add(new PartidoWriteRequest(1, 1, norte, sur, i + 1, i + 1, SABADO_18.plusDays(i)));
        }

        assertThat(ejecutarEnSimultaneo(solicitudes)).isEqualTo(1);
        assertThat(partidoRepository.count()).isEqualTo(1);
    }

    /**
     * Lanza todas las solicitudes a la vez y cuenta cuántas se programaron. Un rechazo por regla de negocio
     * ({@link ConflictoException}) es el resultado esperado para las demás; cualquier otra excepción hace
     * fallar la prueba.
     */
    private int ejecutarEnSimultaneo(List<PartidoWriteRequest> solicitudes) throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(solicitudes.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (PartidoWriteRequest solicitud : solicitudes) {
                Callable<Boolean> tarea = () -> {
                    largada.await();
                    try {
                        partidoService.programar(solicitud);
                        return true;
                    } catch (ConflictoException rechazo) {
                        return false;
                    }
                };
                resultados.add(hilos.submit(tarea));
            }
            largada.countDown();
            int programados = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get(30, TimeUnit.SECONDS)) {
                    programados++;
                }
            }
            return programados;
        } finally {
            hilos.shutdownNow();
        }
    }

    private int crearEquipo(String nombre) {
        return equipoRepository.save(new Equipo(1, nombre, null)).getId();
    }
}