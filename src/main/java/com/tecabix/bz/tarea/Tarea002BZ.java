package com.tecabix.bz.tarea;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import com.tecabix.db.repository.TareaRepository;
import com.tecabix.res.b.RSB042;
import com.tecabix.sv.rq.RQSV049;

public class Tarea002BZ {

	private final TareaRepository tareaRepository;
	
	public Tarea002BZ(TareaRepository tareaRepository) {
		this.tareaRepository = tareaRepository;
	}

	public ResponseEntity<RSB042> listar(final RQSV049 rqsv049) {
		RSB042 rsb042 = rqsv049.getRsb042();
		byte elementos = rqsv049.getElementos();
		short pagina = rqsv049.getPagina();
		
	    String texto = rqsv049.getTexto().orElse(null);
	    List<UUID> estatus = rqsv049.getEstatus();
	    List<UUID> prioridad = rqsv049.getPrioridad();
	    List<UUID> tipoBacklog = rqsv049.getTipoBacklog();
	    LocalDate fechaMin = rqsv049.getFechaCreacionMin();
	    LocalDate fechaMax = rqsv049.getFechaCreacionMax();
	    UUID trabajador = rqsv049.getTrabajador().orElse(null);
	    if(texto == null) {
	    	texto = new String();
	    }
	    Sort sort = Sort.by(Sort.Direction.ASC, "id");
	    Pageable pageable = PageRequest.of(pagina, elementos, sort);
	    if(rqsv049.getTrabajador().isPresent()) {
	    	return rsb042.ok(tareaRepository.findByFilterTrabajador(texto, estatus, prioridad, fechaMin, fechaMax, tipoBacklog, trabajador, pageable));
	    }
	    return rsb042.ok(tareaRepository.findByFilter(texto, estatus, prioridad, fechaMin, fechaMax, tipoBacklog, pageable));
	}
}
