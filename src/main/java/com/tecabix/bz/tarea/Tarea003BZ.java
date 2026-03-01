package com.tecabix.bz.tarea;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.PersonaFisica;
import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.Usuario;
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

	public Tarea003BZ(TareaRepository tareaRepository, UsuarioRepository usuarioRepository,
			PersonaFisicaRepository personaFisicaRepository) {
		this.tareaRepository = tareaRepository;
		this.usuarioRepository = usuarioRepository;
		this.personaFisicaRepository = personaFisicaRepository;
	}

	public ResponseEntity<RSB043> detalle(final RQSV050 rqsv050) {
		RSB043 respose = rqsv050.getRsb043();
		String ticket = rqsv050.getTicket();
		long id = Long.parseLong(ticket.replace("TAR-", ""));

		Optional<Tarea> tareaOp = tareaRepository.findById(id);
		if (tareaOp.isEmpty()) {
			return respose.notFound("No se encontro el registro");
		}

		Tarea tarea = tareaOp.get();

		Map<Byte, String> nombres = new HashMap<Byte, String>();

		PersonaFisica persona = tarea.getTrabajador().getPersonaFisica();
		String nombre = persona.getNombre();
		if (persona.getApellidoPaterno() != null && persona.getApellidoMaterno() != null) {
			if (!persona.getApellidoPaterno().isBlank() && !persona.getApellidoMaterno().isBlank()) {
				nombre = nombre + " " + persona.getApellidoPaterno() + " " + persona.getApellidoMaterno();
			}
		}
		nombres.put(RSA033.RESPONSABLE, nombre);
		nombres.put(RSA033.ID_RESPONSABLE, persona.getPersona().getClave().toString());

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
		nombres.put(RSA033.CREADOR, nombre);
		nombres.put(RSA033.ID_CREADOR, persona.getPersona().getClave().toString());

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
		nombres.put(RSA033.MODIFICADOR, nombre);
		nombres.put(RSA033.ID_MODIFICADOR, persona.getPersona().getClave().toString());

		return respose.ok(tarea, nombres);
	}
}