package com.tecabix.bz.tarea;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.CatalogoTipo;
import com.tecabix.db.entity.Proyecto;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.TareaComentario;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.entity.Usuario;
import com.tecabix.db.repository.ProyectoRepository;
import com.tecabix.db.repository.TareaComentarioRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.db.repository.UsuarioRepository;
import com.tecabix.res.b.RSB045;
import com.tecabix.sv.rq.RQSV052;

/**
 *
 * @author Ramirez Urrutia Angel Abinadi
 */
public class Tarea005BZ {

	private final TrabajadorRepository trabajadorRepository;
	private final ProyectoRepository proyectoRepository;
	private final TareaRepository tareaRepository;
	private final TareaComentarioRepository tareaComentarioRepository;
	private final UsuarioRepository usuarioRepository;

	private final CatalogoTipo prioridad;
	private final CatalogoTipo tipoBacklog;

	public Tarea005BZ(TrabajadorRepository trabajadorRepository, ProyectoRepository proyectoRepository,
			TareaRepository tareaRepository, TareaComentarioRepository tareaComentarioRepository,
			UsuarioRepository usuarioRepository, CatalogoTipo prioridad, CatalogoTipo tipoBacklog) {
		super();
		this.trabajadorRepository = trabajadorRepository;
		this.proyectoRepository = proyectoRepository;
		this.tareaRepository = tareaRepository;
		this.tareaComentarioRepository = tareaComentarioRepository;
		this.usuarioRepository = usuarioRepository;
		this.prioridad = prioridad;
		this.tipoBacklog = tipoBacklog;
	}

	public ResponseEntity<RSB045> actualizar(final RQSV052 rqsv052) {

		RSB045 rsb045 = rqsv052.getRsb045();
		Sesion sesion = rqsv052.getSesion();

		Optional<Tarea> tareaOp = tareaRepository.findByClave(rqsv052.getTarea());
		if (tareaOp.isEmpty()) {
			return rsb045.notFound("No se encontro la tarea");
		}

		Tarea tarea = tareaOp.get();

		StringBuilder builder = new StringBuilder(
				"El usuario [" + sesion.getUsuario().getNombre() + "|" + sesion.getUsuario().getClave() + "]");
		StringBuilder cambio = new StringBuilder();

		// Nombre
		if (rqsv052.getNombre() != null && !rqsv052.getNombre().equals(tarea.getNombre())) {
			cambio.append(", cambio el nombre");
			tarea.setNombre(rqsv052.getNombre());
		}

		// Descripción
		if (rqsv052.getDescripcion() != null && !rqsv052.getDescripcion().equals(tarea.getDescripcion())) {
			cambio.append(", cambio la descripcion");
			tarea.setDescripcion(rqsv052.getDescripcion());
		}

		// Tiempo estimado
		if (rqsv052.getTiempoEstimado() != tarea.getTiempoEstimado()) {
			cambio.append(", cambio el tiempo estimado");
			tarea.setTiempoEstimado(rqsv052.getTiempoEstimado());
		}

		// Tiempo transcurrido
		if (rqsv052.getTiempoTranscurrido() != tarea.getTiempoTranscurrido()) {
			cambio.append(", cambio el tiempo transcurrido");
			tarea.setTiempoTranscurrido(rqsv052.getTiempoTranscurrido());
		}

		// Prioridad (Catalogo)
		if (rqsv052.getPrioridad() != null && !rqsv052.getPrioridad().equals(tarea.getPrioridad().getClave())) {
			cambio.append(", cambio la prioridad");
			Optional<Catalogo> optional = prioridad.getCatalogos().stream()
					.filter(x -> x.getClave().equals(rqsv052.getPrioridad())).findAny();
			if (optional.isEmpty()) {
				return rsb045.notFound("No se encontro la prioridad a actualizar");
			}
			tarea.setPrioridad(optional.get());
		}

		// Tipo backlog (Catalogo)
		if (rqsv052.getTipoBacklog() != null && !rqsv052.getTipoBacklog().equals(tarea.getTipoBacklog().getClave())) {
			cambio.append(", cambio el tipo backlog");
			Optional<Catalogo> optional = tipoBacklog.getCatalogos().stream()
					.filter(x -> x.getClave().equals(rqsv052.getTipoBacklog())).findAny();
			if (optional.isEmpty()) {
				return rsb045.notFound("No se encontro el tipo backlog a actualizar");
			}
			tarea.setTipoBacklog(optional.get());
		}

		// Responsable (Trabajador)
		if (rqsv052.getTrabajador() != null && !rqsv052.getTrabajador().equals(tarea.getTrabajador().getClave())) {
			cambio.append(", cambio el responsable");
			Optional<Trabajador> optionalResponsable = trabajadorRepository.findByClave(rqsv052.getTrabajador());
			if (optionalResponsable.isEmpty()) {
				return rsb045.notFound("No se encontro el responsable a actualizar");
			}
			tarea.setTrabajador(optionalResponsable.get());
		}

		// Proyecto
		if (rqsv052.getProyecto() != null && !rqsv052.getProyecto().equals(tarea.getProyecto().getClave())) {
			cambio.append(", cambio el proyecto");
			Optional<Proyecto> proyectoOp = proyectoRepository.findByClave(rqsv052.getProyecto());
			if (proyectoOp.isEmpty()) {
				return rsb045.notFound("No se encontro el proyecto a actualizar");
			}
			tarea.setProyecto(proyectoOp.get());
		}

		if (cambio.isEmpty()) {
			return rsb045.badRequest("No hay cambios");
		}

		builder.append(cambio);

		tarea.setIdUsuarioModificado(sesion.getUsuario().getId());
		tarea.setFechaModificado(LocalDateTime.now());
		tareaRepository.save(tarea);

		Usuario usuario = usuarioRepository.findById(sesion.getUsuario().getId())
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		TareaComentario comentario = new TareaComentario();
		comentario.setFechaModificado(LocalDateTime.now());
		comentario.setUsuarioCreador(sesion.getUsuario().getId());
		comentario.setClave(UUID.randomUUID());
		comentario.setComentario(builder.toString());
		comentario.setFechaCreacion(LocalDateTime.now());
		comentario.setFechaModificado(LocalDateTime.now());
		comentario.setIdUsuarioModificado(sesion.getUsuario().getId());
		comentario.setUsuario(usuario);
		comentario.setIdTarea(tarea.getId());
		comentario.setEstatus(tarea.getEstatus());
		tareaComentarioRepository.save(comentario);

		return rsb045.ok(tarea);
	}
}