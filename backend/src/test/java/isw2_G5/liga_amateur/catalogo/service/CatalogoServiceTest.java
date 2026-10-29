package isw2_G5.liga_amateur.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import isw2_G5.liga_amateur.catalogo.dto.ArbitroResponse;
import isw2_G5.liga_amateur.catalogo.dto.CanchaResponse;
import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import isw2_G5.liga_amateur.catalogo.repository.ArbitroRepository;
import isw2_G5.liga_amateur.catalogo.repository.CanchaRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la conversión a DTO y que solo se consulten registros activos, sin Spring ni H2. */
class CatalogoServiceTest {

    private CanchaRepository canchaRepository;
    private ArbitroRepository arbitroRepository;
    private CatalogoService service;

    @BeforeEach
    void setUp() {
        canchaRepository = mock(CanchaRepository.class);
        arbitroRepository = mock(ArbitroRepository.class);
        service = new CatalogoService(canchaRepository, arbitroRepository);
    }

    @Test
    void convierteLasCanchasActivasADto() {
        when(canchaRepository.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(new Cancha("Complejo El Alero", "Pje. Los Ceibos 1350", true)));

        List<CanchaResponse> canchas = service.listarCanchasActivas();

        assertThat(canchas).containsExactly(
                new CanchaResponse(null, "Complejo El Alero", "Pje. Los Ceibos 1350", true));
        verify(canchaRepository).findByActivoTrueOrderByNombreAsc();
        verifyNoMoreInteractions(canchaRepository);
    }

    @Test
    void convierteLosArbitrosActivosADto() {
        when(arbitroRepository.findByActivoTrueOrderByApellidoAscNombreAsc())
                .thenReturn(List.of(new Arbitro("Paula", "Godoy", "32444555", true)));

        List<ArbitroResponse> arbitros = service.listarArbitrosActivos();

        assertThat(arbitros).containsExactly(
                new ArbitroResponse(null, "Paula", "Godoy", "32444555", true));
        verify(arbitroRepository).findByActivoTrueOrderByApellidoAscNombreAsc();
        verifyNoMoreInteractions(arbitroRepository);
    }

    @Test
    void devuelveListaVaciaSiNoHayRegistrosActivos() {
        when(canchaRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of());

        assertThat(service.listarCanchasActivas()).isEmpty();
    }
}