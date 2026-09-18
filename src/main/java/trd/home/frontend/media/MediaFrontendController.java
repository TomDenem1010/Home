package trd.home.frontend.media;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import trd.home.common.logging.LogMethodCall;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.media.service.MediaService;

@Controller
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaFrontendController {
    private final MediaService mediaService;
    private final FrontendPageRenderer pageRenderer;

    @GetMapping
    @LogMethodCall
    public String index() {
        return "redirect:/media/by-actor";
    }

    @GetMapping("/server-folder-path")
    @LogMethodCall
    public String serverFolderPath(Model model) {
        return pageRenderer.render(
                model,
                "/media/server-folder-path",
                "MEDIA / Server folder path",
                "Enter a server folder path to import videos.",
                "media/server-folder-path");
    }

    @GetMapping("/by-actor")
    @LogMethodCall
    public String byActor(@RequestParam(required = false) String actorId, Model model) {
        model.addAttribute("actors", mediaService.activeActors());
        model.addAttribute("videos", actorId == null ? List.of() : mediaService.videosByActor(actorId));
        model.addAttribute("selectedId", actorId);
        return page(model, "by-actor", "ByActor");
    }

    @GetMapping("/by-folder")
    @LogMethodCall
    public String byFolder(@RequestParam(required = false) String folderId, Model model) {
        model.addAttribute("folders", mediaService.activeFolders());
        model.addAttribute("videos", folderId == null ? List.of() : mediaService.videosByFolder(folderId));
        model.addAttribute("selectedId", folderId);
        return page(model, "by-folder", "ByFolder");
    }

    @PostMapping("/import")
    @LogMethodCall
    public String importPath(@RequestParam String path) {
        mediaService.importPath(path);
        return "redirect:/media/server-folder-path";
    }

    @GetMapping("/videos/{id}/stream")
    public ResponseEntity<Resource> stream(@PathVariable String id) throws IOException {
        Resource resource = mediaService.videoResource(id);
        MediaType type = MediaTypeFactory.getMediaType(resource)
                .filter(mediaType -> "video".equals(mediaType.getType()))
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(type)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }

    private String page(Model model, String mode, String title) {
        model.addAttribute("mode", mode);
        return pageRenderer.render(
                model,
                "/media/" + mode,
                title,
                "Select an actor or folder, then choose a video to play.",
                "media/library");
    }
}
