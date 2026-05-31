package com.tecabix.bz.tarea;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Actividad;
import com.tecabix.db.entity.PersonaFisica;
import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.Usuario;
import com.tecabix.db.repository.ActividadRepository;
import com.tecabix.db.repository.PersonaFisicaRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.db.repository.UsuarioRepository;
import com.tecabix.res.a.RSA033;
import com.tecabix.res.b.RSB043;
import com.tecabix.sv.rq.RQSV050;

/**
*
* @author Ramirez Urrutia Angel Abinadi
*/
public class Tarea003BZ {

	private final TareaRepository tareaRepository;
	private final UsuarioRepository usuarioRepository;
	private final PersonaFisicaRepository personaFisicaRepository;
	private final ActividadRepository actividadRepository;

	public Tarea003BZ(TareaRepository tareaRepository, UsuarioRepository usuarioRepository,
			PersonaFisicaRepository personaFisicaRepository, ActividadRepository actividadRepository) {
		super();
		this.tareaRepository = tareaRepository;
		this.usuarioRepository = usuarioRepository;
		this.personaFisicaRepository = personaFisicaRepository;
		this.actividadRepository = actividadRepository;
	}


	public ResponseEntity<RSB043> detalle(final RQSV050 rqsv050) {
		RSB043 respose = rqsv050.getRsb043();
		String ticket = rqsv050.getTicket();
		long id = Long.parseLong(ticket.replace("TASK-", ""));

		Optional<Tarea> tareaOp = tareaRepository.findById(id);
		if (tareaOp.isEmpty()) {
			return respose.notFound("No se encontro el registro");
		}

		Tarea tarea = tareaOp.get();

		Map<Byte, String> map = new HashMap<Byte, String>();

		PersonaFisica persona = tarea.getTrabajador().getPersonaFisica();
		String nombre = persona.getNombre();
		if (persona.getApellidoPaterno() != null && persona.getApellidoMaterno() != null) {
			if (!persona.getApellidoPaterno().isBlank() && !persona.getApellidoMaterno().isBlank()) {
				nombre = nombre + " " + persona.getApellidoPaterno() + " " + persona.getApellidoMaterno();
			}
		}
		map.put(RSA033.RESPONSABLE, nombre);
		map.put(RSA033.ID_RESPONSABLE, tarea.getTrabajador().getClave().toString());

		Optional<Usuario> usuarioOptional = usuarioRepository.findById(tarea.getIdUsuarioCreador());
		if (usuarioOptional.isEmpty()) {
			return respose.notFound("No se encontro el usuario que creo el ticket");
		}

		Optional<PersonaFisica> personaFisicaOP = personaFisicaRepository
				.findByPersona(usuarioOptional.get().getUsuarioPersona().getPersona().getId());
		if (personaFisicaOP.isEmpty()) {
			return respose.notFound("No se encontro la persona que creo el ticket");
		}

		persona = personaFisicaOP.get();
		nombre = persona.getNombre();
		if (persona.getApellidoPaterno() != null && persona.getApellidoMaterno() != null) {
			if (!persona.getApellidoPaterno().isBlank() && !persona.getApellidoMaterno().isBlank()) {
				nombre = nombre + " " + persona.getApellidoPaterno() + " " + persona.getApellidoMaterno();
			}
		}
		map.put(RSA033.CREADOR, nombre);
		map.put(RSA033.ID_CREADOR, persona.getPersona().getClave().toString());

		usuarioOptional = usuarioRepository.findById(tarea.getIdUsuarioModificado());
		if (usuarioOptional.isEmpty()) {
			return respose.notFound("No se encontro el usuario que modifico el ticket");
		}

		personaFisicaOP = personaFisicaRepository
				.findByPersona(usuarioOptional.get().getUsuarioPersona().getPersona().getId());
		if (personaFisicaOP.isEmpty()) {
			return respose.notFound("No se encontro la persona que modifico el ticket");
		}

		persona = personaFisicaOP.get();
		nombre = persona.getNombre();
		if (persona.getApellidoPaterno() != null && persona.getApellidoMaterno() != null) {
			if (!persona.getApellidoPaterno().isBlank() && !persona.getApellidoMaterno().isBlank()) {
				nombre = nombre + " " + persona.getApellidoPaterno() + " " + persona.getApellidoMaterno();
			}
		}
		map.put(RSA033.MODIFICADOR, nombre);
		map.put(RSA033.ID_MODIFICADOR, persona.getPersona().getClave().toString());
		
		Actividad actividad = actividadRepository.findByPendiente(tarea.getTrabajador().getId()).orElse(null);
		if(actividad != null && actividad.getTarea().equals(tarea)) {
			map.put(RSA033.ACTIVIDAD, actividad.getClave().toString());
		}
		
		return respose.ok(tarea, map);
	}
}