package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ncpl.bms.reports.model.dto.BuildingDTO;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @GetMapping("/get-building-details/{id}")
    public BuildingDTO getBuildingById(@PathVariable Integer id) {
        return buildingService.getBuildingById(id);
    }
    @PutMapping("/edit-building/{id}")
    public ResponseEntity<BuildingDTO> updateBuilding(@PathVariable Integer id, @RequestBody BuildingDTO buildingDTO) {
        BuildingDTO updatedBuilding = buildingService.updateBuilding(id, buildingDTO);
        return ResponseEntity.ok(updatedBuilding);
    }


}
