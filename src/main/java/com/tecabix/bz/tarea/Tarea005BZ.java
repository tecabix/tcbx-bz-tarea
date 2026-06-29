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
import com.tecabix.db.repository.ProyectoRepository;
import com.tecabix.db.repository.TareaComentarioRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
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

	private final CatalogoTipo prioridad;
	private final CatalogoTipo tipoBacklog;

	public Tarea005BZ(TrabajadorRepository trabajadorRepository, ProyectoRepository proyectoRepository,
			TareaRepository tareaRepository, TareaComentarioRepository tareaComentarioRepository,
			CatalogoTipo prioridad, CatalogoTipo tipoBacklog) {
		super();
		this.trabajadorRepository = trabajadorRepository;
		this.proyectoRepository = proyectoRepository;
		this.tareaRepository = tareaRepository;
		this.tareaComentarioRepository = tareaComentarioRepository;
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

		StringBuilder comentarioCambio = new StringBuilder();

		if (rqsv052.getNombre() != null && !rqsv052.getNombre().equals(tarea.getNombre())) {
			comentarioCambio.append("Se cambió el nombre de ")
					.append(tarea.getNombre())
					.append(" a ")
					.append(rqsv052.getNombre())
					.append(". <br/>");

			tarea.setNombre(rqsv052.getNombre());
		}

		if (rqsv052.getDescripcion() != null && !rqsv052.getDescripcion().equals(tarea.getDescripcion())) {
			comentarioCambio.append("Se cambió la descripción de ")
					.append(tarea.getDescripcion())
					.append(" a ")
					.append(rqsv052.getDescripcion())
					.append(". <br/>");

			tarea.setDescripcion(rqsv052.getDescripcion());
		}

		if (rqsv052.getTiempoEstimado() != tarea.getTiempoEstimado()) {
			comentarioCambio.append("Se cambió el tiempo estimado de ")
					.append(tarea.getTiempoEstimado())
					.append(" a ")
					.append(rqsv052.getTiempoEstimado())
					.append(". <br/>");

			tarea.setTiempoEstimado(rqsv052.getTiempoEstimado());
		}

		if (rqsv052.getPrioridad() != null && !rqsv052.getPrioridad().equals(tarea.getPrioridad().getClave())) {

			Optional<Catalogo> optional = prioridad.getCatalogos().stream()
					.filter(x -> x.getClave().equals(rqsv052.getPrioridad()))
					.findAny();

			if (optional.isEmpty()) {
				return rsb045.notFound("No se encontro la prioridad a actualizar");
			}

			Catalogo prioridadAnterior = tarea.getPrioridad();
			Catalogo prioridadNueva = optional.get();

			comentarioCambio.append("Se cambió la prioridad de ")
					.append(prioridadAnterior.getNombre())
					.append(" a ")
					.append(prioridadNueva.getNombre())
					.append(". <br/>");

			tarea.setPrioridad(prioridadNueva);
		}

		if (rqsv052.getTipoBacklog() != null && !rqsv052.getTipoBacklog().equals(tarea.getTipoBacklog().getClave())) {

			Optional<Catalogo> optional = tipoBacklog.getCatalogos().stream()
					.filter(x -> x.getClave().equals(rqsv052.getTipoBacklog()))
					.findAny();

			if (optional.isEmpty()) {
				return rsb045.notFound("No se encontro el tipo backlog a actualizar");
			}

			Catalogo tipoBacklogAnterior = tarea.getTipoBacklog();
			Catalogo tipoBacklogNuevo = optional.get();

			comentarioCambio.append("Se cambió el tipo backlog de ")
					.append(tipoBacklogAnterior.getNombre())
					.append(" a ")
					.append(tipoBacklogNuevo.getNombre())
					.append(". <br/>");

			tarea.setTipoBacklog(tipoBacklogNuevo);
		}

		if (rqsv052.getTrabajador() != null && !rqsv052.getTrabajador().equals(tarea.getTrabajador().getClave())) {

			Optional<Trabajador> optionalResponsable = trabajadorRepository.findByClave(rqsv052.getTrabajador());

			if (optionalResponsable.isEmpty()) {
				return rsb045.notFound("No se encontro el responsable a actualizar");
			}

			Trabajador responsableAnterior = tarea.getTrabajador();
			Trabajador responsableNuevo = optionalResponsable.get();

			comentarioCambio.append("Se cambió el responsable de ")
					.append(responsableAnterior.getPersonaFisica().getNombre())
					.append(" a ")
					.append(responsableNuevo.getPersonaFisica().getNombre())
					.append(". <br/>");

			tarea.setTrabajador(responsableNuevo);
		}

		if (rqsv052.getProyecto() != null && !rqsv052.getProyecto().equals(tarea.getProyecto().getClave())) {

			Optional<Proyecto> proyectoOp = proyectoRepository.findByClave(rqsv052.getProyecto());

			if (proyectoOp.isEmpty()) {
				return rsb045.notFound("No se encontro el proyecto a actualizar");
			}

			Proyecto proyectoAnterior = tarea.getProyecto();
			Proyecto proyectoNuevo = proyectoOp.get();

			comentarioCambio.append("Se cambió el proyecto de ")
					.append(proyectoAnterior.getNombre())
					.append(" a ")
					.append(proyectoNuevo.getNombre())
					.append(". <br/>");

			tarea.setProyecto(proyectoNuevo);
		}
		
		boolean existeTarea = tareaRepository.findAll()
	        .stream()
            .anyMatch(x ->
                x.getProyecto() != null
                && x.getProyecto().getClave().equals(rqsv052.getProyecto())
                && x.getNombre() != null
                && x.getNombre().equalsIgnoreCase(rqsv052.getNombre())
            );
    
        if (existeTarea) {
            return rsb045.badRequest(
                "Ya existe una tarea con ese nombre en el proyecto.");
        }

		if (comentarioCambio.isEmpty()) {
			return rsb045.badRequest("No hay cambios");
		}

		Trabajador trabajador = trabajadorRepository.findByClaveUsuario(sesion.getUsuario().getClave()).orElse(null);
		if (trabajador == null) {
			return rsb045.notFound("No se encontro el trabajador.");
		}

		tarea.setIdUsuarioModificado(sesion.getUsuario().getId());
		tarea.setFechaModificado(LocalDateTime.now());
		tareaRepository.save(tarea);

		TareaComentario comentario = new TareaComentario();
		comentario.setFechaModificado(LocalDateTime.now());
		comentario.setUsuarioCreador(sesion.getUsuario().getId());
		comentario.setClave(UUID.randomUUID());
		comentario.setComentario(comentarioCambio.toString().trim());
		comentario.setFechaCreacion(LocalDateTime.now());
		comentario.setFechaModificado(LocalDateTime.now());
		comentario.setIdUsuarioModificado(sesion.getUsuario().getId());
		comentario.setTrabajador(trabajador);
		comentario.setIdTarea(tarea.getId());
		comentario.setEstatus(tarea.getEstatus());
		tareaComentarioRepository.save(comentario);

		return rsb045.ok(tarea);
	}
}