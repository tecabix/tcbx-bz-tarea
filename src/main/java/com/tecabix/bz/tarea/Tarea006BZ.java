package com.tecabix.bz.tarea;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.TareaComentario;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.TareaComentarioRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB046;
import com.tecabix.sv.rq.RQSV053;

public class Tarea006BZ {

	private final TareaComentarioRepository tareaComentarioRepository;
	private final TareaRepository tareaRepository;
	private final TrabajadorRepository trabajadorRepository;
	private final Catalogo activo;

	public Tarea006BZ(TareaComentarioRepository tareaComentarioRepository,
			TareaRepository tareaRepository, TrabajadorRepository trabajadorRepository, Catalogo activo) {
		super();
		this.tareaComentarioRepository = tareaComentarioRepository;
		this.tareaRepository = tareaRepository;
		this.trabajadorRepository = trabajadorRepository;
		this.activo = activo;
	}

	public ResponseEntity<RSB046> crearComentario(RQSV053 rqsv053) {
		RSB046 response = rqsv053.getRsb046();
		Sesion sesion = rqsv053.getSesion();
		TareaComentario comentario = new TareaComentario();
		comentario.setComentario(rqsv053.getComentario());

		Optional<Tarea> op = tareaRepository.findByClave(rqsv053.getTarea());
		op.ifPresent(x -> comentario.setIdTarea(x.getId()));

		if(comentario.getIdTarea() == null) {
			return response.notFound("NO SE ENCONTRO LA TAREA");
		}
		

		Trabajador trabajador = trabajadorRepository.findByClaveUsuario(sesion.getUsuario().getClave()).orElse(null);
		if(trabajador == null) {
			return response.notFound("No se encontro el trabjador.");
		}
		
		comentario.setTrabajador(trabajador);
		comentario.setUsuarioCreador(sesion.getUsuario().getId());
		comentario.setClave(UUID.randomUUID());
		comentario.setEstatus(activo);
		comentario.setFechaCreacion(LocalDateTime.now());
		comentario.setFechaModificado(LocalDateTime.now());
		comentario.setIdUsuarioModificado(sesion.getUsuario().getId());
		tareaComentarioRepository.save(comentario);
		return response.ok(comentario);
	}
}