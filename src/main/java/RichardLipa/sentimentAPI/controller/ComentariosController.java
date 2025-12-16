package RichardLipa.sentimentAPI.controller;

import RichardLipa.sentimentAPI.domain.comentario.*;
import RichardLipa.sentimentAPI.domain.usuario.*;
import RichardLipa.sentimentAPI.service.ComentarioService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@RestController
@RequestMapping("/comentarios")
public class ComentariosController {
    @Autowired
    private ComentarioService comentarioService;
@Autowired
 private IUsuarioRepository usuarioRepository;
@Autowired
private IComentarioRepository comentarioRepository;



    @Transactional
    @PostMapping
    public ResponseEntity registroComentario(@RequestBody @Valid DatosRegistroComentario datos, UriComponentsBuilder uriComponentsBuilder){


        Optional<Usuario> usuarioOptional = usuarioRepository.findById(datos.id_usuario());

        if(usuarioOptional.isPresent() ){
            System.out.println("usuario: ==> "+ usuarioOptional + " get : " + usuarioOptional.get());
            var comentario = new Comentario(datos, usuarioOptional.get());

           comentarioRepository.save(comentario);
            var uri = uriComponentsBuilder.path("comentario/{id}").buildAndExpand(comentario.getId()).toUri();

            return ResponseEntity.created(uri).body(new DatosDetalleComentario(comentario));
        }else{
            String mensaje = "⚠️ ⚠️El ID Usuario " + datos.id_usuario() + " no corresponde a ningún tópico registrado.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mensaje);
        }


    }

    @GetMapping
    public  ResponseEntity<Page<DatosListaComentarios>> listarComentarios(@PageableDefault(size = 20, sort = {"comentario"}, page = 0) Pageable paginacion){
        //var page = comentarioRepository.findAllByStateTrue(paginacion).map(DatosListaComentarios:: new);
        Page<DatosListaComentarios> page = comentarioService.obtenerComentariosPaginados(paginacion);
        //var listacomentarios = ResponseEntity.ok(page);
        return  ResponseEntity.ok(page);

    }
}



