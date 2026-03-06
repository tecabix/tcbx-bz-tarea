package com.tecabix.bz.tarea;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Tarea;
import com.tecabix.db.entity.TareaComentario;
import com.tecabix.db.repository.TareaComentarioRepository;
import com.tecabix.db.repository.TareaRepository;
import com.tecabix.res.b.RSB047;
import com.tecabix.sv.rq.RQSV054;

public class Tarea007BZ {

	private final TareaRepository tareaRepository;
	private final TareaComentarioRepository tareaComentarioRepository;

	public Tarea007BZ(TareaRepository tareaRepository, TareaComentarioRepository tareaComentarioRepository) {
		this.tareaRepository = tareaRepository;
		this.tareaComentarioRepository = tareaComentarioRepository;
	}

	public ResponseEntity<RSB047> listarComentario(RQSV054 rqsv054) {
		RSB047 response = rqsv054.getRsb047();

		Optional<Tarea> optional = tareaRepository.findByClave(rqsv054.getIdTarea());
		if (!optional.isPresent()) {
			return response.notFound("No se encontró la tarea.");
		}

		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		Pageable pageable = PageRequest.of(rqsv054.getPagina(), rqsv054.getElementos(), sort);

		Page<TareaComentario> pages = tareaComentarioRepository.findByIdTarea(optional.get().getId(), pageable);
		return response.ok(pages);
	}
}