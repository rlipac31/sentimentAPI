package RichardLipa.sentimentAPI.controller;


import RichardLipa.sentimentAPI.domain.comentario.DatosListaComentarios;
import RichardLipa.sentimentAPI.domain.comentario.RespuestaClasificacion;
import RichardLipa.sentimentAPI.service.ComentarioService;
import RichardLipa.sentimentAPI.service.IAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analizar")
public class AnalizadorDeComentariosController {

    @Autowired
    private ComentarioService comentarioService;
    @Autowired
    private IAService iaService;

        @Autowired
        private ObjectMapper objectMapper;  // Spring lo inyecta automáticamente
        @GetMapping(value = "/analizar-comentarios", produces = MediaType.APPLICATION_JSON_VALUE)
        public RespuestaClasificacion analizandoComentarios(
                @PageableDefault(size = 20, page = 1, sort = {"id"}) Pageable paginacion) {
            // 1. Obtener comentarios paginados (como objetos Java)
            List<DatosListaComentarios> listaComentarios =
                    comentarioService.obtenerComentariosPaginados(paginacion)
                            .getContent();

            String jsonRespuestaIA = iaService.clasificarComentarios(listaComentarios);

            System.out.println("=== RESPUESTA CRUDA DE LA IA ===");
            System.out.println(jsonRespuestaIA);
            System.out.println("=== FIN RESPUESTA CRUDA ===");

            try {

                Map<String, List<DatosListaComentarios>> mapaRespuesta =
                        objectMapper.readValue(jsonRespuestaIA, new TypeReference<Map<String, List<DatosListaComentarios>>>() {});
                List<DatosListaComentarios> comentariosClasificados = mapaRespuesta.get("content");
                // 4. Devolver respuesta estructurada
                return new RespuestaClasificacion(
                        true,
                        listaComentarios.size(),
                        comentariosClasificados
                );

            } catch (JsonProcessingException e) {
                // En caso de error al parsear (JSON mal formado)
                return new RespuestaClasificacion(
                        false,
                        listaComentarios.size(),
                        null,
                        "Error al procesar la respuesta de la IA: " + e.getMessage()
                );
            }
        }
    }




