package com.hwn.sw_project.controller;

import com.hwn.sw_project.service.gov24.Gov24SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/gov24")
public class Gov24SyncController {
    private final Gov24SyncService syncService;

    @PostMapping("/sync-servicelist")
    public String syncServiceList(){
        syncService.syncAllFromApi();
        return "ok";
    }
}
