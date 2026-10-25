package com.novasalud.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.novasalud.backend.dto.SedeRequest;
import com.novasalud.backend.entity.Sede;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.SedeRepository;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class SedeService {

    private final SedeRepository sedeRepository;
    
    public SedeService(SedeRepository sedeRepository){
        this.sedeRepository=sedeRepository;
    }

    @Transactional(readOnly = true)
    public List<Sede>listarSedes(){
        return sedeRepository.findAll();
    }

    @Transactional 
    public Sede crearSede(SedeRequest request){
        if(sedeRepository.existsByNombre(request.getNombre())){
            throw new RuntimeException("Ya existe una sede con el nombre "+ request.getNombre());
        }

        Sede sede = new Sede();
        sede.setNombre(request.getNombre());
        sede.setDireccion(request.getDireccion());
        sede.setTelefono(request.getTelefono());
        sede.setEstado(EstadoGenerico.ACTIVO);

        return sedeRepository.save(sede);
    }

    @Transactional 
    public Sede actualizarSede(Long id, SedeRequest request){
        Sede sede = sedeRepository.findById(id).orElseThrow(() -> new RuntimeException("Sede no encontrada"));
        if(!sede.getNombre().equals(request.getNombre()) && sedeRepository.existsByNombre(request.getNombre())){
            throw new RuntimeException("Ya existe otra sede con el nombre: " + request.getNombre());
        }
        sede.setNombre(request.getNombre());
        sede.setDireccion(request.getDireccion());
        sede.setTelefono(request.getTelefono());

        return sedeRepository.save(sede);
    }

    @Transactional 
    public void desactivarSede(Long id){
        Sede sede = sedeRepository.findById(id).orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + id));
        sede.setEstado(EstadoGenerico.INACTIVO);
        sedeRepository.save(sede);
    }
}
