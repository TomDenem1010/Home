package trd.home.frontend.media;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.*;
import trd.home.media.service.*;

@Controller
@RequestMapping("/media")
public class MediaFrontendController {
    private final MediaService mediaService;

    public MediaFrontendController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    @LogMethodCall
    public String index() {
        return "redirect:/media/by-actor";
    }

    @GetMapping("/server-folder-path")
    @LogMethodCall
    public String serverFolderPath(Model model) {
        model.addAttribute("activePath", "/media/server-folder-path");
        model.addAttribute("pageTitle", "MEDIA / Server folder path");
        model.addAttribute("pageContent", "Enter a server folder path to import videos.");
        model.addAttribute("contentTemplate", "media/server-folder-path");
        return "index";
    }

    @GetMapping("/by-actor")
    @LogMethodCall
    public String byActor(@RequestParam(required = false) String actorId, Model model) {
        model.addAttribute("actors", mediaService.activeActors());
        model.addAttribute("videos", actorId == null ? java.util.List.of() : mediaService.videosByActor(actorId));
        model.addAttribute("selectedId", actorId);
        return page(model, "by-actor", "ByActor");
    }

    @GetMapping("/by-folder")
    @LogMethodCall
    public String byFolder(@RequestParam(required = false) String folderId, Model model) {
        model.addAttribute("folders", mediaService.activeFolders());
        model.addAttribute("videos", folderId == null ? java.util.List.of() : mediaService.videosByFolder(folderId));
        model.addAttribute("selectedId", folderId);
        return page(model, "by-folder", "ByFolder");
    }

    @PostMapping("/import")
    @LogMethodCall
    public String importPath(@RequestParam String path, RedirectAttributes redirect) {
        redirect.addFlashAttribute("importMessage", "Imported videos: " + mediaService.importPath(path));
        return "redirect:/media/server-folder-path";
    }

    @GetMapping("/api/actors")
    @ResponseBody
    @LogMethodCall
    public List<ActorDto> actors() {
        return mediaService.activeActors();
    }

    @GetMapping("/api/folders")
    @ResponseBody
    @LogMethodCall
    public List<FolderDto> folders() {
        return mediaService.activeFolders();
    }

    @GetMapping("/api/videos")
    @ResponseBody
    @LogMethodCall
    public List<VideoDto> videos() {
        return mediaService.activeVideos();
    }

    @GetMapping("/api/actors/{id}/videos")
    @ResponseBody
    @LogMethodCall
    public List<VideoDto> actorVideos(@PathVariable String id) {
        return mediaService.videosByActor(id);
    }

    @GetMapping("/api/folders/{id}/videos")
    @ResponseBody
    @LogMethodCall
    public List<VideoDto> folderVideos(@PathVariable String id) {
        return mediaService.videosByFolder(id);
    }

    @PostMapping("/api/import")
    @ResponseBody
    @LogMethodCall
    public Map<String, Integer> importApi(@RequestParam String path) throws IOException {
        return Map.of("imported", mediaService.importPath(path));
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
        model.addAttribute("activePath", "/media/" + mode);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageContent", "Select an actor or folder, then choose a video to play.");
        model.addAttribute("contentTemplate", "media/library");
        return "index";
    }
}
