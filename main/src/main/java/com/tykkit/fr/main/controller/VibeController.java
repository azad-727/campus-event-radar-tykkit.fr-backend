package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.model.Vibe;
import com.tykkit.fr.main.repository.VibeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vibes")
@CrossOrigin(origins = "http://localhost:5173")
public class VibeController {
    @Autowired
    VibeRepository vibeRepo;

    @GetMapping
    public ResponseEntity<List<Vibe>> getAllVibes(){
            return ResponseEntity.ok(vibeRepo.findAll());
    }
    @PostMapping("/{id}/increment")
    public ResponseEntity<Vibe> incrementVibe(@PathVariable String id){
        Vibe vibe = vibeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vibe not found"));
        vibe.setVotes(vibe.getVotes()+1);
        return ResponseEntity.ok(vibeRepo.save(vibe));
    }
    @PostMapping("/bulk")
    public ResponseEntity<List<Vibe>> createIntialVibes(@RequestBody List<Vibe> vibes){
        return ResponseEntity.ok(vibeRepo.saveAll(vibes));
    }
}
