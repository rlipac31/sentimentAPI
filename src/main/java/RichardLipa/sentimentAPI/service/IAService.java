package RichardLipa.sentimentAPI.service;


import RichardLipa.sentimentAPI.domain.comentario.DatosListaComentarios;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IAService {


    private final ChatClient chatClient;
    private final ObjectMapper objectMapper; // <-- Objeto para manejar JSON

    // Inyección de ChatClient y ObjectMapper
    public IAService (ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }



    /**
     * Convierte la lista de objetos y llama al modelo de IA para clasificarla.
     * @param listaComentarios Lista de Records Java.
     * @return Una cadena JSON con los comentarios clasificados.
     */
    public String clasificarComentarios(List<DatosListaComentarios> listaComentarios) {
        System.out.println(" lista " + listaComentarios);/// SI SE GENERA LA LISTA
    //CREO QUE SOLO LLEGA HASTA AQUI
        String comentariosJson;
        try {
            // Convertimos la lista de Records a una cadena JSON limpia
            comentariosJson = objectMapper.writeValueAsString(listaComentarios);
            System.out.println(" comentariosJSON  "+ comentariosJson );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar la lista de comentarios a JSON", e);
        }

        var instrucion = """
            Eres un clasificador de sentimientos experto.
            
            Tu ÚNICA tarea es analizar la lista de comentarios que te paso en formato JSON y devolver EXACTAMENTE el mismo formato pero con el campo "tipo" rellenado como: POSITIVO, NEGATIVO o NEUTRO (en mayúsculas).
            
            REGLAS OBLIGATORIAS:
            - NO uses bloques de código Markdown (nada de ```json ni ```)
            - NO agregues explicaciones, texto adicional, saludos ni comentarios
            - Devuelve SOLO el objeto JSON puro, nada más
            - Mantén exactamente los valores de "id", "comentario", "usuario" y "fecharegistro"
            
            Formato exacto de salida:
            {
              "content": [
                {
                  "id": 1,
                  "comentario": "texto original",
                  "tipo": "POSITIVO",
                  "usuario": "email",
                  "fecharegistro": "fecha original"
                }
                // ... todos los comentarios
              ]
            }
            """;

        try {

            String respuestaIA = this.chatClient.prompt()
                    .system(instrucion)
                    .user(comentariosJson) // <-- Le pasamos el JSON perfecto
                    .options(ChatOptions.builder().temperature(0.4).build())
                    .call()
                    .content();


            System.out.println("=== RESPUESTA CRUDA DE LA IA ===");
            System.out.println(respuestaIA);
            System.out.println("=== FIN RESPUESTA ===");
          return  respuestaIA;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al clasificar comentarios con el modelo de IA", e);
        }


    }
}