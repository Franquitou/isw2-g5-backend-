package isw2_G5.liga_amateur.catalogo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Prueba las consultas contra el {@code schema.sql} y {@code data.sql} reales, sobre una H2 en memoria.
 * {@code @DataJpaTest} reemplaza el datasource por una base embebida: no toca {@code ./data/liga_amateur}.
 * {@code ddl-auto=validate} hace fallar la prueba si las entidades no coinciden con las tablas del esquema.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
class CatalogoRepositoryTest {

    @Autowired
    private CanchaRepository canchaRepository;

    @Autowired
    private ArbitroRepository arbitroRepository;

    @Test
    void listaLasCanchasPrecargadasOrdenadasPorNombre() {
        List<Cancha> canchas = canchaRepository.findByActivoTrueOrderByNombreAsc();

        assertThat(canchas).extracting(Cancha::getNombre)
                .containsExactly("Cancha Municipal Norte", "Complejo El Alero", "Predio La Costanera");
        assertThat(canchas.get(0).getId()).isEqualTo(1);
        assertThat(canchas.get(0).getDireccion()).isEqualTo("Av. Blas Parera 7400");
    }

    @Test
    void excluyeLasCanchasInactivas() {
        canchaRepository.saveAndFlush(new Cancha("Cancha Cerrada", null, false));

        assertThat(canchaRepository.findByActivoTrueOrderByNombreAsc())
                .extracting(Cancha::getNombre)
                .doesNotContain("Cancha Cerrada")
                .hasSize(3);
    }

    @Test
    void listaLosArbitrosPrecargadosOrdenadosPorApellido() {
        List<Arbitro> arbitros = arbitroRepository.findByActivoTrueOrderByApellidoAscNombreAsc();

        assertThat(arbitros).extracting(Arbitro::getApellido)
                .containsExactly("Bustos", "Ferreyra", "Godoy", "Quiroga");
        assertThat(arbitros.get(0).getNombre()).isEqualTo("Hernán");
        assertThat(arbitros.get(0).getDni()).isEqualTo("27333444");
    }

    @Test
    void excluyeLosArbitrosInactivos() {
        arbitroRepository.saveAndFlush(new Arbitro("Ana", "Acosta", "40111222", false));

        assertThat(arbitroRepository.findByActivoTrueOrderByApellidoAscNombreAsc())
                .extracting(Arbitro::getApellido)
                .doesNotContain("Acosta")
                .hasSize(4);
    }
}