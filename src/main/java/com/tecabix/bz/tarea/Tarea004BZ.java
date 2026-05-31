package com.tecabix.bz.tarea;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.TareaComentario;
import com.tecabix.db.entity.Usuario;
import com.tecabix.db.repository.CatalogoRepository;
import com.tecabix.db.repository.TareaComentarioRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.res.b.RSB044;
import com.tecabix.sv.rq.RQSV051;

/**
 *
 * @author Ramirez Urrutia Angel Abinadi
 */
public class Tarea004BZ {

	private final CatalogoRepository catalogoRepository;
	private final TareaRepository tareaRepository;
	private final TareaComentarioRepository tareaComentarioRepository;

	private final Catalogo porHacer;
	private final Catalogo enProceso;
	private final Catalogo enRevision;
	private final Catalogo listo;
	private final Catalogo enPausa;
	private final Catalogo bloqueado;
	private final Catalogo conObservaciones;

	private Usuario usuario;

	public Tarea004BZ(
			CatalogoRepository catalogoRepository,
			TareaRepository tareaRepository,
			TareaComentarioRepository tareaComentarioRepository,
			Catalogo porHacer,
			Catalogo enProceso,
			Catalogo enRevision,
			Catalogo listo,
			Catalogo enPausa,
			Catalogo bloqueado,
			Catalogo conObservaciones,
			Usuario usuario) {

		this.catalogoRepository = catalogoRepository;
		this.tareaRepository = tareaRepository;
		this.tareaComentarioRepository = tareaComentarioRepository;
		this.porHacer = porHacer;
		this.enProceso = enProceso;
		this.enRevision = enRevision;
		this.listo = listo;
		this.enPausa = enPausa;
		this.bloqueado = bloqueado;
		this.conObservaciones = conObservaciones;
		this.usuario = usuario;
	}

	public ResponseEntity<RSB044> actualizarEstatus(final RQSV051 rqsv051) {

		RSB044 rsb044 = rqsv051.getRsb044();
		Sesion sesion = rqsv051.getSesion();

		Optional<Catalogo> estatusOp = catalogoRepository.findByClave(rqsv051.getEstatus());
		if (estatusOp.isEmpty()) {
			return rsb044.notFound("No se encontro el estatus");
		}

		Optional<Tarea> tareaOp = tareaRepository.findByClave(rqsv051.getTarea());
		if (tareaOp.isEmpty()) {
			return rsb044.notFound("No se encontro la tarea");
		}

		Catalogo estatus = estatusOp.get();
		Tarea tarea = tareaOp.get();

		if (estatus.equals(porHacer)) {

			if (!tarea.getEstatus().equals(enProceso)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else if (estatus.equals(enProceso)) {

			if (!tarea.getEstatus().equals(porHacer)
					&& !tarea.getEstatus().equals(conObservaciones)
					&& !tarea.getEstatus().equals(enPausa)
					&& !tarea.getEstatus().equals(bloqueado)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else if (estatus.equals(enRevision)) {

			if (!tarea.getEstatus().equals(enProceso)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else if (estatus.equals(listo)) {

			if (!tarea.getEstatus().equals(enRevision)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else if (estatus.equals(enPausa)) {

			if (!tarea.getEstatus().equals(enProceso)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else if (estatus.equals(bloqueado)) {

			if (!tarea.getEstatus().equals(enProceso)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else if (estatus.equals(conObservaciones)) {

			if (!tarea.getEstatus().equals(enRevision)) {
				return rsb044.badRequest("No se puede cambiar el estatus.");
			}

		} else {

			return rsb044.badRequest("No se puede cambiar el estatus.");
		}

		String estatusViejo = tarea.getEstatus().getNombre();

		tarea.setEstatus(estatus);
		String estatusNuevo = tarea.getEstatus().getNombre();

		tarea.setIdUsuarioModificado(sesion.getUsuario().getId());
		tarea.setFechaModificado(LocalDateTime.now());

		tareaRepository.save(tarea);

		TareaComentario comentario = new TareaComentario();

		comentario.setClave(UUID.randomUUID());
		comentario.setComentario("El usuario [" 
				+ sesion.getUsuario().getNombre() 
				+ "|" 
				+ sesion.getUsuario().getClave() 
				+ "] cambio el estatus de " 
				+ estatusViejo 
				+ " a " 
				+ estatusNuevo + ".");

		comentario.setFechaCreacion(LocalDateTime.now());
		comentario.setFechaModificado(LocalDateTime.now());

		comentario.setUsuario(usuario);
		comentario.setIdTarea(tarea.getId());

		comentario.setUsuarioCreador(sesion.getUsuario().getId());
		comentario.setIdUsuarioModificado(sesion.getUsuario().getId());

		comentario.setEstatus(tarea.getEstatus());

		tareaComentarioRepository.save(comentario);

		return rsb044.ok(tarea);
	}
}