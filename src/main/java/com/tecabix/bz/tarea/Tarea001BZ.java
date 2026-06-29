package com.tecabix.bz.tarea;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.CatalogoTipo;
import com.tecabix.db.entity.Proyecto;
import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.ProyectoRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB036;
import com.tecabix.sv.rq.RQSV048;

/**
*
* @author Ramirez Urrutia Angel Abinadi
*/
public class Tarea001BZ {

	private final ProyectoRepository proyectoRepository;
	private final TrabajadorRepository trabajadorRepository;
	private final TareaRepository tareaRepository;
	private final Catalogo porHacer;
	private final CatalogoTipo tipoPrioridad;
	private final CatalogoTipo tipoBacklog;
	
	
	public Tarea001BZ(ProyectoRepository proyectoRepository, TrabajadorRepository trabajadorRepository,
			TareaRepository tareaRepository, Catalogo porHacer, CatalogoTipo tipoPrioridad, CatalogoTipo tipoBacklog) {

		this.proyectoRepository = proyectoRepository;
		this.trabajadorRepository = trabajadorRepository;
		this.tareaRepository = tareaRepository;
		this.porHacer = porHacer;
		this.tipoPrioridad = tipoPrioridad;
		this.tipoBacklog = tipoBacklog;
	}


	public ResponseEntity<RSB036> crear(final RQSV048 rqsv048) {
		RSB036 response = rqsv048.getRsb036();
		Tarea tarea = new Tarea();
		
		Optional<Trabajador> trabajadorOP = trabajadorRepository.findByClave(rqsv048.getTrabajadorId());
		if(trabajadorOP.isEmpty()) {
			return response.notFound("No se encontro el trabajador");
		}
		tarea.setTrabajador(trabajadorOP.get());
		
		Optional<Catalogo> catalogoOP = tipoPrioridad.getCatalogos().stream().filter(x->x.getClave().equals(rqsv048.getPrioridadId())).findAny();
		if(catalogoOP.isEmpty()) {
			return response.notFound("No se encontro la prioridad");
		}
		tarea.setPrioridad(catalogoOP.get());
		if(!tarea.getPrioridad().getCatalogoTipo().equals(tipoPrioridad)) {
			return response.notFound("La prioridad no es valida");
		}
		Optional<Proyecto> proyectoOP = proyectoRepository.findByClave(rqsv048.getProyectoId());
		if(proyectoOP.isEmpty()) {
			return response.notFound("No se encontro el proyecto");
		}

		boolean existeTarea = tareaRepository.findAll()
	        .stream()
	        .anyMatch(x ->
	            x.getProyecto() != null
	            && x.getProyecto().getClave().equals(rqsv048.getProyectoId())
	            && x.getNombre() != null
	            && x.getNombre().equalsIgnoreCase(rqsv048.getNombre())
	        );

	    if (existeTarea) {
	        return response.badRequest(
	            "Ya existe una tarea con ese nombre en el proyecto.");
	    }

		tarea.setProyecto(proyectoOP.get());
		Optional<Catalogo> tipoBacklogOP = tipoBacklog.getCatalogos().stream().filter(x->x.getClave().equals(rqsv048.getTipoBacklogId())).findAny();
		if(tipoBacklogOP.isEmpty()) {
			return response.notFound("No se encontro el tipo de backlog");
		}
		tarea.setTipoBacklog(tipoBacklogOP.get());
		tarea.setClave(UUID.randomUUID());
		tarea.setDescripcion(rqsv048.getDescripcion());
		tarea.setEstatus(porHacer);
		tarea.setFechaCreacion(LocalDateTime.now());
		tarea.setFechaModificado(LocalDateTime.now());
		tarea.setIdUsuarioModificado(rqsv048.getSesion().getUsuario().getId());
		tarea.setIdUsuarioCreador(rqsv048.getSesion().getUsuario().getId());
		tarea.setNombre(rqsv048.getNombre());
		tarea.setTiempoEstimado(rqsv048.getTiempoEstimado());
		return response.ok(tareaRepository.save(tarea));
	}
}
